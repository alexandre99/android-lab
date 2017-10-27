package br.com.agenda.agenda;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by italo.teixeira on 27/10/2017.
 */

public class Localizador implements GoogleApiClient.ConnectionCallbacks, LocationListener {


    private final GoogleApiClient client;
    private final GoogleMap googleMap;

    public Localizador(Context context, GoogleMap googleMap) {
        client = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

        client.connect();

        this.googleMap = googleMap;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest request = new LocationRequest();
        //infoma para o gps que só atualiza as informações do gps a cada 50m
        request.setSmallestDisplacement(50);
        //atualiza os dados do gps a cada um segundo
        request.setInterval(1000);
        //Prioridade da precisão
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng coordenada = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(coordenada);
        googleMap.moveCamera(cameraUpdate);
    }
}

