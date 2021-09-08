package br.com.uberclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import br.com.uberclone.R;
import br.com.uberclone.config.ConfiguracaoFirebase;
import br.com.uberclone.helper.UsuarioFirebase;
import br.com.uberclone.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private EditText campoEmail, campoSenha, campoNome;
    private Switch switchTipoUsuario;
    private Button btnCadastrar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        startComponents();

        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarCamposUsuario();
            }
        });

    }

    private void startComponents() {
        campoNome = findViewById(R.id.editCadastroNome);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        switchTipoUsuario = findViewById(R.id.switchCadastro);
        btnCadastrar = findViewById(R.id.btnCadastrar);
    }

    public void validarCamposUsuario(){

        //Recuperar texto dos campos
        String textoNome = campoNome.getText().toString();
        String textoSenha= campoSenha.getText().toString();
        String textoEmail = campoEmail.getText().toString();

        if (!textoNome.isEmpty()){
            if (!textoEmail.isEmpty()){
                if (!textoSenha.isEmpty()){

                    Usuario usuario = new Usuario();
                    usuario.setNome(textoNome);
                    usuario.setEmail(textoEmail);
                    usuario.setSenha(textoSenha);
                    usuario.setTipo(validarTipoUsuario());
                    
                    cadastrarUsuario( usuario );

                }else{
                    campoSenha.setError(getString(R.string.errorSingUp));
                }
            }else {
                campoEmail.setError(getString(R.string.errorSingUp));
            }
        }else {
            campoNome.setError(getString(R.string.errorSingUp));
        }
    }

    private void cadastrarUsuario(Usuario usuario) {

        auth = ConfiguracaoFirebase.getFirebaseAuth();
        auth.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    try {
                        String idUsuario = task.getResult().getUser().getUid();
                        usuario.setId(idUsuario);
                        usuario.salvar();

                        //Atualizar nome no UserProfile;
                        UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                        //Redirecionar o usurario a partir do seu tipo
                        //Se for um passageiro abrirá  a activity maps
                        //Senão a activity requisições
                        if (validarTipoUsuario() == "P"){
                            startActivity(new Intent(CadastroActivity.this,MapsActivity.class));
                            finish();
                            Toast.makeText(CadastroActivity.this, R.string.SucessCadastroPassageiro, Toast.LENGTH_SHORT).show();
                        }else {
                            startActivity(new Intent(getApplicationContext(),RequisicoesActivity.class));
                            finish();
                            Toast.makeText(CadastroActivity.this, R.string.SucessCadastroMotorista, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    String excessao = "";
                    try {
                        throw task.getException();
                    }catch(FirebaseAuthWeakPasswordException e){
                        excessao = getString(R.string.ExceptionWeakPassword);
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excessao = getString(R.string.ExceptionInvalidCredentials);
                    }catch (FirebaseAuthUserCollisionException e){
                        excessao = getString(R.string.ExceptionUserCollision);
                    }catch (Exception e){
                        excessao = getString(R.string.ExceptionGeralRegistroUsuario) + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroActivity.this, excessao, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public String validarTipoUsuario(){
        return switchTipoUsuario.isChecked() ? "M" : "P" ;
    }
}