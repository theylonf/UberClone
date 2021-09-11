package br.com.uberclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import org.jetbrains.annotations.NotNull;

import br.com.uberclone.R;
import br.com.uberclone.config.ConfiguracaoFirebase;
import br.com.uberclone.helper.UsuarioFirebase;
import br.com.uberclone.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    private EditText campo_email, campo_senha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Inicializar componentes
        campo_email = findViewById(R.id.editLoginEmail);
        campo_senha = findViewById(R.id.editLoginSenha);
    }

    public void validarCampoLogin(View view) {
        //Recuperar texto dos campos
        String texto_email = campo_email.getText().toString();
        String texto_senha = campo_senha.getText().toString();

        if (!texto_email.isEmpty()){//verifica e-mail
            if (!texto_senha.isEmpty()){//verifica senha
                Usuario usuario = new Usuario();
                usuario.setEmail(texto_email);
                usuario.setSenha(texto_senha);

                logarUsuario(usuario);

            }else {
                campo_senha.setError(getString(R.string.errorSingUp));
            }
        }else {
            campo_email.setError(getString(R.string.errorSingUp));
        }

    }

    private void logarUsuario(Usuario usuario) {
        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(),usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    UsuarioFirebase.redirecionarUsuarioLogado(LoginActivity.this);
                    finish();
                }else {
                    String excessao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        excessao = "Usuario não cadastrado";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excessao = "Senha incorreta";
                    }catch (Exception e){
                        excessao = getString(R.string.ExceptionGeralRegistroUsuario) + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this, excessao, Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}