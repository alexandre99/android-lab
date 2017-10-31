package br.com.agenda.agenda.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.List;

import br.com.agenda.agenda.WebClient;
import br.com.agenda.agenda.converter.AlunoConverter;
import br.com.agenda.agenda.dao.AlunoDAO;
import br.com.agenda.agenda.modelo.Aluno;

/**
 * Created by italo.teixeira on 25/10/2017.
 */

public class EnviaAlunosTask extends AsyncTask<Object, Object, String> {

    private Context context;
    private ProgressDialog dialog;

    public EnviaAlunosTask(Context context) {
        this.context = context;
    }

    /**
     *
     * @param params qq param que recebe pelo metodo execute, o tipo do parâmetro pode ser trocado pelo generic
     *               1 opção
     * @return resposta do client requisição
     */
    @Override
    protected String doInBackground(Object[] params) {
        AlunoDAO dao = new AlunoDAO(context);
        List<Aluno> alunos = dao.buscaAlunos();
        dao.close();

        AlunoConverter conversor = new AlunoConverter();
        String json = conversor.converterParaJson(alunos);

        WebClient client = new WebClient();
        String resposta = client.post(json);

        return resposta;
    }

    @Override
    protected void onPostExecute(String resposta) {
        dialog.dismiss();
        Toast.makeText(context, resposta, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context, "Aguarde", "Enviando alunos...", true, true);
    }
}
