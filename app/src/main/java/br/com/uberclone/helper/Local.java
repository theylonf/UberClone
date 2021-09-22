package br.com.uberclone.helper;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Local {

    public static float calcularDistancia(LatLng latLngInicial, LatLng latLngFinal){
        Location localInicial = new Location("Local Inicial");
        localInicial.setLatitude(latLngInicial.latitude);
        localInicial.setLongitude(latLngInicial.longitude);

        Location localFinal = new Location("Local Final");
        localFinal.setLatitude(latLngFinal.latitude);
        localFinal.setLongitude(latLngFinal.longitude);

        //Dividir por 1000 para converter pra KM
        float distancia = localInicial.distanceTo(localFinal) / 1000;

        return distancia;

    }

}
