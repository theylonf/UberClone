package br.com.uberclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import br.com.uberclone.R;
import br.com.uberclone.adapter.RequisicoesAdapter;
import br.com.uberclone.config.ConfiguracaoFirebase;
import br.com.uberclone.databinding.ActivityRequisicoesBinding;
import br.com.uberclone.helper.UsuarioFirebase;
import br.com.uberclone.model.Requisicao;
import br.com.uberclone.model.Usuario;

public class RequisicoesActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseReference firebaseRef;
    private ActivityRequisicoesBinding binding;
    private List<Requisicao> listaRequisicao = new ArrayList<>();
    private RequisicoesAdapter adapter;
    private Usuario motorista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inicializarComponentes();

        recuperarRequisicoes();
    }

    private void recuperarRequisicoes() {
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");
        Query requisicaoPesquisa = requisicoes.orderByChild("status")
                .equalTo(Requisicao.STATUS_AGUARDANDO);
        requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() >0){
                    binding.textResultado.setVisibility(View.GONE);
                    binding.recyclerRequisicoes.setVisibility(View.VISIBLE);
                }else {
                    binding.textResultado.setVisibility(View.VISIBLE);
                    binding.recyclerRequisicoes.setVisibility(View.GONE);
                }
                for (DataSnapshot ds: snapshot.getChildren()){
                    Requisicao requisicao = ds.getValue(Requisicao.class);
                    listaRequisicao.add(requisicao);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuSair:
                auth.signOut();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    private void inicializarComponentes() {
        binding = ActivityRequisicoesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle(R.string.requisicoes);

        //Configurações iniciais
        motorista = UsuarioFirebase.getDadosUsuarioAtual();
        auth = ConfiguracaoFirebase.getFirebaseAuth();
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        //Configurando RecyclerView
        adapter = new RequisicoesAdapter(listaRequisicao,getApplicationContext(),motorista);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        binding.recyclerRequisicoes.setLayoutManager(layoutManager);
        binding.recyclerRequisicoes.setHasFixedSize(true);
        binding.recyclerRequisicoes.setAdapter(adapter);

    }
}