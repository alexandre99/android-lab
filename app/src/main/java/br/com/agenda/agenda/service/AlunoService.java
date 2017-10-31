package br.com.agenda.agenda.service;

import java.util.List;

import br.com.agenda.agenda.dto.AlunoSync;
import br.com.agenda.agenda.modelo.Aluno;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by italo.teixeira on 31/10/2017.
 */

public interface AlunoService {

    @POST("aluno")
    Call<Void> insere(@Body Aluno aluno);

    @GET("aluno")
    Call<AlunoSync> lista();
}
