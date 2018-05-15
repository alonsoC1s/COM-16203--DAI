package jcsiglerp.androidproject.buscar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import jcsiglerp.androidproject.comprar.Comprar;
import jcsiglerp.androidproject.historial.Historial;
import jcsiglerp.androidproject.Model.Carrito;
import jcsiglerp.androidproject.Model.Prenda;
import jcsiglerp.androidproject.Model.Usuario;
import jcsiglerp.androidproject.MyApplication;
import jcsiglerp.androidproject.R;

public class Buscar extends AppCompatActivity implements BuscarAdapter.AddToCartClickedListener {

    Carrito carrito;
    EditText etBuscar;
    RecyclerView rvBusqueda;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar);
        etBuscar = (EditText) findViewById(R.id.etBuscar);
        rvBusqueda = (RecyclerView) findViewById(R.id.rvBusqueda);

        etBuscar.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                buscar(Buscar.this.getCurrentFocus());
                return true;
            }
        });

        this.findViewById(R.id.btCarrito).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Buscar.this, Comprar.class);
                intent.putExtras(Buscar.this.getIntent().getExtras());
                startActivity(intent);
            }
        });
        this.findViewById(R.id.btHistorial).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Buscar.this, Historial.class);
                intent.putExtras(Buscar.this.getIntent().getExtras());
                startActivity(intent);
            }
        });

        rvBusqueda.setLayoutManager(new LinearLayoutManager(this));
        rvBusqueda.setAdapter(new BuscarAdapter(this));


        Realm realm = ((MyApplication) getApplication()).getRealm();
        RealmResults < Prenda > results = realm.where(Prenda.class).findAll();

        ((BuscarAdapter)rvBusqueda.getAdapter()).setData(results);

        Bundle b = this.getIntent().getExtras();
        carrito = realm.where(Usuario.class).equalTo("correo", b.get("correo").toString()).findFirst().carrito;
    }

    protected void buscar(View v) {
        Realm realm = ((MyApplication) getApplication()).getRealm();
        String keyWords[] = etBuscar.getText().toString().split(" ");
        List < Prenda > prendaList;
        if(keyWords[0] != "") {
            HashSet<Prenda> prendas = new HashSet<>();
            prendaList = new ArrayList<>();
            for (String kw : keyWords) {
                RealmResults<Prenda> results = realm.where(Prenda.class).equalTo("etiquetas.nombre", kw).findAll();
                for (Prenda p : results)
                    prendas.add(p);
            }
            for(String kw : keyWords) {
                RealmResults<Prenda> results = realm.where(Prenda.class).equalTo("etiquetas.nombre", kw).findAll();
                for (Prenda p : results) {
                    if(prendas.contains(p)) {
                        prendaList.add(p);
                        prendas.remove(p);
                    }
                }
            }
        } else {
            prendaList = realm.where(Prenda.class).findAll();
        }
        ((BuscarAdapter)rvBusqueda.getAdapter()).setData(prendaList);
    }

    @Override
    public void itemClicked(Prenda prenda, int cantidad) {
        Realm realm = ((MyApplication) getApplication()).getRealm();
        realm.beginTransaction();
        for(int i = 0; i < cantidad; ++i)
            carrito.agregarPrenda(prenda);
        realm.commitTransaction();
        Toast.makeText(Buscar.this, "Añadido exitosamente al carrito!", Toast.LENGTH_SHORT).show();
    }
}
