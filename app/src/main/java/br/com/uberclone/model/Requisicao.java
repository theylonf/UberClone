package br.com.uberclone.model;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

import br.com.uberclone.config.ConfiguracaoFirebase;

public class Requisicao {
    private String id, status;
    private Usuario passageiro, motorista;
    private Destino destino;

    public static final String STATUS_AGUARDANDO = "aguardando";
    public static final String STATUS_A_CAMINHO = "a_caminho";
    public static final String STATUS_VIAGEM = "viagem";
    public static final String STATUS_FINALIZDA = "finalizada";
    public static final String STATUS_ENCERRADA = "encerrada";

    public Requisicao() {
    }

    public void salvarRequisicao(){
        DatabaseReference reference = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = reference.child("requisicoes");

        String idRequisicao = requisicoes.push().getKey();
        setId(idRequisicao);

        requisicoes.child(getId()).setValue(this);
    }

    public void atualizar(){
        DatabaseReference reference = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = reference.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId());
        Map map = new HashMap();
        map.put("motorista",getMotorista());
        map.put("status", getStatus());

        requisicao.updateChildren(map);

    }

    public void atualizaStatus(){
        DatabaseReference reference = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = reference.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId());
        Map map = new HashMap();
        map.put("status", getStatus());

        requisicao.updateChildren(map);

    }

    public void atualizarLocalizacaoMotorista(){
        DatabaseReference reference = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = reference.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId())
                .child("motorista");
        Map map = new HashMap();
        map.put("latitude",motorista.getLatitude());
        map.put("longitude", motorista.getLongitude());

        requisicao.updateChildren(map);

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Usuario getPassageiro() {
        return passageiro;
    }

    public void setPassageiro(Usuario passageiro) {
        this.passageiro = passageiro;
    }

    public Usuario getMotorista() {
        return motorista;
    }

    public void setMotorista(Usuario motorista) {
        this.motorista = motorista;
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) {
        this.destino = destino;
    }
}
