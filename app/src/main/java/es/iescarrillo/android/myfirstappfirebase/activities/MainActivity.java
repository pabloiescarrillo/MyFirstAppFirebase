package es.iescarrillo.android.myfirstappfirebase.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.iescarrillo.android.myfirstappfirebase.R;
import es.iescarrillo.android.myfirstappfirebase.adapters.PersonAdapter;
import es.iescarrillo.android.myfirstappfirebase.models.Person;

public class MainActivity extends AppCompatActivity {

    private ListView lvPersons;
    private Button btnAddPerson;
    private List<Person> persons;
    private PersonAdapter personAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadComponents();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("persons");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                persons.clear();

                snapshot.getChildren().forEach(person -> {
                    Log.i("Persona lambda", Objects.requireNonNull(person.getValue(Person.class)).toString());
                    persons.add(person.getValue(Person.class));
                });

                personAdapter = new PersonAdapter(getApplicationContext(), persons);
                lvPersons.setAdapter(personAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error firebase", error.toString());
            }
        });

        btnAddPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InsertOrEditPersonActivity.class);
                // Indicamos que NO estamos en modo edición
                intent.putExtra("editMode", false);
                startActivity(intent);
                finish();
            }
        });

        lvPersons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Obtengo el objeto person que he seleccionado en el ListView
                Person person = persons.get(position);

                Intent intent = new Intent(MainActivity.this, InsertOrEditPersonActivity.class);
                intent.putExtra("person", person);
                // Indicamos que estamos en modo edición
                intent.putExtra("editMode", true);
                startActivity(intent);
                finish();
            }
        });

    }

    private void loadComponents(){
        lvPersons = findViewById(R.id.lvPersons);
        btnAddPerson = findViewById(R.id.btnAddPerson);
        persons = new ArrayList<>();
    }
}