package br.com.agenda.agenda;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import br.com.agenda.agenda.adapter.AlunosAdapter;
import br.com.agenda.agenda.dao.AlunoDAO;
import br.com.agenda.agenda.event.AtualizaListaAlunoEvent;
import br.com.agenda.agenda.modelo.Aluno;
import br.com.agenda.agenda.retrofit.RetrofitInicializador;
import br.com.agenda.agenda.sinc.AlunoSincronizador;
import br.com.agenda.agenda.task.EnviaAlunosTask;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.*;

public class ListaAlunoActivity extends AppCompatActivity {

    private final AlunoSincronizador sincronizador = new AlunoSincronizador(this);
    private ListView listaAlunos;

    public static final Integer CODIGO_SMS = 9;
    private SwipeRefreshLayout swipe;
    private ProgressDialog progess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_aluno);

        EventBus eventBus = EventBus.getDefault();
        eventBus.register(this);


        listaAlunos = (ListView) findViewById(R.id.lista_alunos);

        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe_lista_aluno);

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sincronizador.buscarTodos();
                sincronizador.sincronizaAlunosInternos();
            }
        });

        listaAlunos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> lista, View item, int position, long id) {
                Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(position);
                Intent intentVaiProFormulario = new Intent(ListaAlunoActivity.this, FormularioActivity.class);
                intentVaiProFormulario.putExtra("aluno", aluno);
                startActivity(intentVaiProFormulario);
            }
        });

        Button novoAluno = (Button) findViewById(R.id.novo_aluno);
        novoAluno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentVaiParaFormulario = new Intent(ListaAlunoActivity.this, FormularioActivity.class);
                startActivity(intentVaiParaFormulario);
            }
        });

        registerForContextMenu(listaAlunos);

        sincronizador.buscarTodos();

        if (ActivityCompat.checkSelfPermission(ListaAlunoActivity.this, RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ListaAlunoActivity.this, new String[]{RECEIVE_SMS}, CODIGO_SMS);
        }

        sincronizador.sincronizaAlunosInternos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLista();
    }

    private void buscaAlunos() {
        sincronizador.buscarTodos();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista_alunos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_enviar_notas:
                new EnviaAlunosTask(this).execute();
                break;
            case R.id.menu_baixar_provas:
                Intent vaiParaProvas = new Intent(this, ProvasActivity.class);
                startActivity(vaiParaProvas);
                break;
            case R.id.menu_mapa:
                Intent vaiParaMapa = new Intent(this, MapaActivity.class);
                startActivity(vaiParaMapa);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void carregarLista() {
        AlunoDAO dao = new AlunoDAO(this);
        List<Aluno> alunos = dao.buscaAlunos();

        for (Aluno aluno : alunos) {
            Log.i("aluno sincronizado", String.valueOf(aluno.getSincronizado()));
        }

        dao.close();
        AlunosAdapter adapter = new AlunosAdapter(this, alunos);
        listaAlunos.setAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, final ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        final Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(info.position);

        MenuItem itemLigar = menu.add("Ligar");
        itemLigar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (ActivityCompat.checkSelfPermission(ListaAlunoActivity.this, CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ListaAlunoActivity.this,
                            new String[]{CALL_PHONE},
                            123);
                } else {
                    Intent intentLigar = new Intent(Intent.ACTION_CALL);
                    intentLigar.setData(Uri.parse("tel:" + aluno.getTelefone()));
                    startActivity(intentLigar);
                }
                return false;
            }
        });

        MenuItem itemSms = menu.add("Enviar SMS");
        Intent intentSms = new Intent(Intent.ACTION_VIEW);
        intentSms.setData(Uri.parse("sms:" + aluno.getTelefone()));
        itemSms.setIntent(intentSms);

        MenuItem itemMapa = menu.add("Visualizar no mapa");
        Intent intentMapa = new Intent(Intent.ACTION_VIEW);
        intentMapa.setData(Uri.parse("geo:0,0?q=" + aluno.getEndereco()));
        itemMapa.setIntent(intentMapa);

        MenuItem itemSite = menu.add("Visitar site");
        Intent intentSite = new Intent(Intent.ACTION_VIEW);

        String site = aluno.getSite();
        if (!site.startsWith("http://")) {
            site = "http://" + site;
        }

        intentSite.setData(Uri.parse(site));
        itemSite.setIntent(intentSite);

        MenuItem deletar = menu.add("Deletar");
        deletar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                progess = ProgressDialog.show(ListaAlunoActivity.this, "Aguarde", "Apagando aluno");
                Call<Void> call = new RetrofitInicializador().getAlunoService().deleta(aluno.getId());
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        AlunoDAO dao = new AlunoDAO(ListaAlunoActivity.this);
                        dao.deleta(aluno);
                        dao.close();
                        carregarLista();
                        progess.dismiss();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        progess.dismiss();
                        Toast.makeText(ListaAlunoActivity.this, "Não foi possível remover o aluno", Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void atualizaListaAlunoEvent(AtualizaListaAlunoEvent event) {
        if (swipe.isRefreshing()) swipe.setRefreshing(false);
        carregarLista();
    }
}
