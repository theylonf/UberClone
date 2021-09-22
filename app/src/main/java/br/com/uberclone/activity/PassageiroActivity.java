package br.com.uberclone.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import br.com.uberclone.R;
import br.com.uberclone.config.ConfiguracaoFirebase;
import br.com.uberclone.databinding.ActivityPassageiroBinding;
import br.com.uberclone.helper.Local;
import br.com.uberclone.helper.UsuarioFirebase;
import br.com.uberclone.model.Destino;
import br.com.uberclone.model.Requisicao;
import br.com.uberclone.model.Usuario;

public class PassageiroActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityPassageiroBinding binding;
    private FirebaseAuth auth;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localPassageiro, localMotorista, localDestino;
    private boolean uberChamado = false;

    private DatabaseReference firebaseRef;
    private Requisicao requisicao;
    private Usuario passageiro, motorista;
    private String statusRequisicao;
    private Destino destino;
    private Marker marcadorMotorista, marcadorPassageiro, marcadorDestino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inicializarComponentes();

        //Adicionar listener para status de requisição
        verificaStatusRequisicao();

    }

    private void verificaStatusRequisicao() {
        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioAtual();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");
        Query requisicoesPesquisa = requisicoes.orderByChild("passageiro/id")
                .equalTo(usuarioLogado.getId());
        requisicoesPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                List<Requisicao> list = new ArrayList<>();
                for (DataSnapshot ds: snapshot.getChildren()){
                    requisicao = ds.getValue(Requisicao.class);
                    list.add(requisicao);
                }
                Collections.reverse(list);
                if (list != null && list.size()>0){
                    requisicao = list.get(0);

                    if(requisicao != null){
                        passageiro = requisicao.getPassageiro();
                        localPassageiro = new LatLng(
                                Double.parseDouble(passageiro.getLatitude()),
                                Double.parseDouble(passageiro.getLongitude())
                        );
                        statusRequisicao = requisicao.getStatus();
                        destino = requisicao.getDestino();
                        if (requisicao.getMotorista() != null){
                            motorista = requisicao.getMotorista();
                            localMotorista = new LatLng(
                                    Double.parseDouble(motorista.getLatitude()),
                                    Double.parseDouble(motorista.getLongitude())
                            );
                        }
                        alteraInterfaceStatusRequisicao(statusRequisicao);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void alteraInterfaceStatusRequisicao(String status){

        if (status != null && !status.isEmpty()){
            switch ( status ){
                case Requisicao.STATUS_AGUARDANDO :
                    requisicaoAguardando();
                    break;
                case Requisicao.STATUS_A_CAMINHO :
                    requisicaoACaminho();
                    break;
                case Requisicao.STATUS_VIAGEM:
                    requisicaoViagem();
                    break;
                case Requisicao.STATUS_FINALIZDA:
                    requisicaoFinalizada();
                    break;
            }
        }
    }

    private void requisicaoAguardando(){
        binding.linearLayoutDestino.setVisibility(View.GONE);
        binding.btnChamarUber.setText(R.string.cancelUber);
        uberChamado = true;

        //Adicionar marcador passageiro
        adicionaMarcadorPassageiro(localPassageiro,passageiro.getNome());
        centralizarMarcador(localPassageiro);
    }
    private void requisicaoACaminho(){

        binding.linearLayoutDestino.setVisibility(View.GONE);
        binding.btnChamarUber.setText(R.string.driver_is_comming);
        uberChamado = true;

        //Adicionar marcador passageiro
        adicionaMarcadorPassageiro(localPassageiro,passageiro.getNome());

        //Adicionar marcador motorista
        adicionaMarcadorMotorista(localMotorista,motorista.getNome());

        //Centralizar dois marcadores
        centralizarDoisMarcadores(marcadorMotorista,marcadorPassageiro);

    }
    private void requisicaoFinalizada(){
        binding.linearLayoutDestino.setVisibility(View.GONE);


        //Adicionar marcador destino
        localDestino = new LatLng(
                Double.parseDouble(destino.getLatitude()),
                Double.parseDouble(destino.getLongitude())
        );
        adicionaMarcadorDestino(localDestino,getString(R.string.destination));
        centralizarMarcador(localDestino);

        //Calcular Distancia
        float distancia = Local.calcularDistancia(localPassageiro,localDestino);
        float valor = (float) (distancia * 4);
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String resultado  = decimalFormat.format(valor);
        binding.btnChamarUber.setText(getString(R.string.finishRace) + " R$ "+ resultado);

    }
    private void requisicaoViagem(){
        binding.linearLayoutDestino.setVisibility(View.GONE);
        binding.btnChamarUber.setText(R.string.to_destination);

        adicionaMarcadorMotorista(localMotorista,motorista.getNome());

        //Adicionar marcador destino
        localDestino = new LatLng(
                Double.parseDouble(destino.getLatitude()),
                Double.parseDouble(destino.getLongitude())
        );
        adicionaMarcadorDestino(localDestino,getString(R.string.destination));

        //Centralizar dois marcadores
        centralizarDoisMarcadores(marcadorMotorista,marcadorDestino);
    }
    private void adicionaMarcadorPassageiro(LatLng localizacao, String titulo){
        if( marcadorPassageiro != null )
            marcadorPassageiro.remove();

        marcadorPassageiro = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
        );
    }

    private void adicionaMarcadorMotorista(LatLng localizacao, String titulo){

        if( marcadorMotorista != null )
            marcadorMotorista.remove();

        marcadorMotorista = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
        );

    }

    private void adicionaMarcadorDestino(LatLng localizacao, String titulo){
        if( marcadorPassageiro != null )
            marcadorPassageiro.remove();

        if( marcadorDestino != null )
            marcadorDestino.remove();

        marcadorDestino = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destino))
        );

    }

    private void centralizarMarcador(LatLng local){
        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(local, 20)
        );
    }

    private void centralizarDoisMarcadores(Marker marcador1, Marker marcador2){

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include( marcador1.getPosition() );
        builder.include( marcador2.getPosition() );

        LatLngBounds bounds = builder.build();

        int largura = getResources().getDisplayMetrics().widthPixels;
        int altura = getResources().getDisplayMetrics().heightPixels;
        int espacoInterno = (int) (largura * 0.20);

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngBounds(bounds,largura,altura,espacoInterno)
        );

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        recuperarLocalizacaoUsuario();
    }

    public void chamarUber(View view) {

        if (!uberChamado){
            String textoDestino = binding.editDestino.getText().toString();

            if (confirmadoLocalizacaoUsuario()){
                if (!textoDestino.equals("") || textoDestino != null){
                    Address addressDestino = recuperarEndereco(textoDestino);
                    if (addressDestino != null){
                        Destino destino = new Destino();
                        destino.setCidade(addressDestino.getAdminArea());
                        destino.setCep(addressDestino.getPostalCode());
                        destino.setBairro(addressDestino.getSubLocality());
                        destino.setRua(addressDestino.getThoroughfare());
                        destino.setNumero(addressDestino.getFeatureName());

                        destino.setLatitude(String.valueOf(addressDestino.getLatitude()));
                        destino.setLongitude(String.valueOf(addressDestino.getLongitude()));

                        StringBuilder menssagem = new StringBuilder();
                        menssagem.append(getString(R.string.cityDestiny)+ destino.getCidade());
                        menssagem.append(getString(R.string.ThoroughfareDestiny)+ destino.getRua());
                        menssagem.append(getString(R.string.subLocalityDestiny)+destino.getBairro());
                        menssagem.append(getString(R.string.NumberLocality)+destino.getNumero());
                        menssagem.append(getString(R.string.postalCodeDestiny)+destino.getNumero());

                        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                                .setTitle(getString(R.string.confirmAddrs))
                                .setMessage(menssagem)
                                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Salvar requisição
                                        salvarRequisicao(destino);
                                        uberChamado = true;
                                    }
                                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }else {
                    Toast.makeText(this, R.string.setDestinoAddrs, Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(this, R.string.waitingLocation, Toast.LENGTH_SHORT).show();
            }
        }else {
            //Cancelar requisição
            uberChamado = false;

        }
    }

    private void salvarRequisicao(Destino destino) {
        Requisicao requisicao = new Requisicao();
        requisicao.setDestino(destino);
        Usuario usuarioPassageiro = UsuarioFirebase.getDadosUsuarioAtual();
        usuarioPassageiro.setLatitude(String.valueOf(localPassageiro.latitude));
        usuarioPassageiro.setLongitude(String.valueOf(localPassageiro.longitude));

        requisicao.setPassageiro(usuarioPassageiro);
        requisicao.setStatus(Requisicao.STATUS_AGUARDANDO);
        requisicao.salvarRequisicao();

    }

    private Address recuperarEndereco(String endereco){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaEnderecos = geocoder.getFromLocationName(endereco,1);
            if (listaEnderecos != null && listaEnderecos.size() > 0){
                Address address = listaEnderecos.get(0);

                return address;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean confirmadoLocalizacaoUsuario(){
        if (localPassageiro != null){
            return true;
        }else return false;
    }

    private void recuperarLocalizacaoUsuario() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                //Recuperar latitude e longitude
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localPassageiro = new LatLng(latitude, longitude);

                //Atualizar Geofire
                UsuarioFirebase.atualizarDadosLocalizacao(latitude,longitude);

                //Alterar interface de acordo com o status
                alteraInterfaceStatusRequisicao(statusRequisicao);

                if (statusRequisicao != null && !statusRequisicao.isEmpty()){
                    if (statusRequisicao.equals(Requisicao.STATUS_VIAGEM)
                            || statusRequisicao.equals(Requisicao.STATUS_FINALIZDA)){
                        locationManager.removeUpdates(locationListener);
                    }
                }
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //Solicitar atualizacoes de localização
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    10,
                    locationListener
            );
        }


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

        binding = ActivityPassageiroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle(getString(R.string.startRun));

        //Configurações iniciais
        auth = ConfiguracaoFirebase.getFirebaseAuth();
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }
}