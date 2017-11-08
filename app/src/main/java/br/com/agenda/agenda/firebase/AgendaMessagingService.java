package br.com.agenda.agenda.firebase;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Map;

import br.com.agenda.agenda.dao.AlunoDAO;
import br.com.agenda.agenda.dto.AlunoSync;
import br.com.agenda.agenda.event.AtualizaListaAlunoEvent;
import br.com.agenda.agenda.sinc.AlunoSincronizador;

/**
 * Created by italo.teixeira on 01/11/2017.
 */

public class AgendaMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> mensagem = remoteMessage.getData();
        Log.i("mensagem recebida", String.valueOf(mensagem));

        converteParaAluno(mensagem);
    }

    private void converteParaAluno(Map<String, String> mensagem) {
        String chaveDeAcesso = "alunoSync";
        if(mensagem.containsKey(chaveDeAcesso)){
            String json = mensagem.get(chaveDeAcesso);
            ObjectMapper mapper = new ObjectMapper();
            try {
                AlunoSync alunoSync = mapper.readValue(json, AlunoSync.class);

                new AlunoSincronizador(AgendaMessagingService.this).sincroniza(alunoSync);

                AlunoDAO dao = new AlunoDAO(this);
                dao.sincronismo(alunoSync.getAlunos());
                dao.close();

                EventBus eventBust = EventBus.getDefault();
                eventBust.post(new AtualizaListaAlunoEvent());
            } catch (IOException e) {
                Log.e("Erro ao conv. alunosync", e.getMessage());
            }
        }
    }
}
