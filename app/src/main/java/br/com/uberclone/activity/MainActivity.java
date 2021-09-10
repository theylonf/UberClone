package br.com.uberclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import java.security.Permissions;

import br.com.uberclone.R;
import br.com.uberclone.config.Permissoes;
import br.com.uberclone.helper.UsuarioFirebase;

public class MainActivity extends AppCompatActivity {

    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        Permissoes.validarPermissoes(permissoes,this,1);


    }

    public void abrirTelaLogin(View view) {
        startActivity(new Intent(this,LoginActivity.class));
    }

    public void abrirTelaCadastro(View view) {
        startActivity(new Intent(this,CadastroActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        UsuarioFirebase.redirecionarUsuarioLogado(MainActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults){
            if (permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidarPermissao();
            }
        }
    }

    private void alertaValidarPermissao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissãoes Negadas");
        builder.setMessage("Para utilizar o app é necessario aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}