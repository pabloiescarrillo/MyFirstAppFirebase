package es.iescarrillo.android.myfirstappfirebase.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import es.iescarrillo.android.myfirstappfirebase.R;
import es.iescarrillo.android.myfirstappfirebase.models.Person;
import es.iescarrillo.android.myfirstappfirebase.services.PersonService;

public class InsertOrEditPersonActivity extends AppCompatActivity {

    // Componentes de la pantalla
    private Button btnSave, btnCancel, btnDelete;
    private EditText etName, etSurname, etAge;

    // Variables que recibimos en el Intent
    private Person personEdit;
    private Boolean editMode;

    // Servicios que vamos a usar en el Activity
    private PersonService personService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_insert_or_edit_person);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadComponents();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InsertOrEditPersonActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editMode){
                    personEdit.setName(etName.getText().toString());
                    personEdit.setSurname(etSurname.getText().toString());

                    if (etName.getText().toString().isBlank()) {
                        Toast.makeText(InsertOrEditPersonActivity.this, "Name is blank", Toast.LENGTH_SHORT).show();
                        return; // Para salir del listener
                    }
                    if (etSurname.getText().toString().isBlank()) {
                        Toast.makeText(InsertOrEditPersonActivity.this, "Surname is blank", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!etAge.getText().toString().isBlank())
                        personEdit.setAge(Integer.valueOf(etAge.getText().toString()));

                    personService.update(personEdit);

                    Toast.makeText(InsertOrEditPersonActivity.this, "Person updated", Toast.LENGTH_SHORT).show();
                    Log.i("Person id", personEdit.getId());

                    Intent intent = new Intent(InsertOrEditPersonActivity.this, MainActivity.class);
                    startActivity(intent);

                } else {
                    // Creamos la nueva persona y seteamos los valores
                    Person person = new Person();
                    person.setName(etName.getText().toString());
                    person.setSurname(etSurname.getText().toString());

                    if (etName.getText().toString().isBlank()) {
                        Toast.makeText(InsertOrEditPersonActivity.this, "Name is blank", Toast.LENGTH_SHORT).show();
                        return; // Para salir del listener
                    }
                    if (etSurname.getText().toString().isBlank()) {
                        Toast.makeText(InsertOrEditPersonActivity.this, "Surname is blank", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!etAge.getText().toString().isBlank())
                        person.setAge(Integer.valueOf(etAge.getText().toString()));

                    String idPerson = personService.insert(person);
                    Toast.makeText(InsertOrEditPersonActivity.this, "Person with id " + idPerson + " inserted", Toast.LENGTH_SHORT).show();
                    Log.i("Person id", idPerson);

                    Intent intent = new Intent(InsertOrEditPersonActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                personService.delete(personEdit);
                Toast.makeText(getApplicationContext(), "Successfully removed", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(InsertOrEditPersonActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loadComponents(){
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etAge = findViewById(R.id.etAge);

        personService = new PersonService(getApplicationContext());

        Intent intent = getIntent();
        if(intent.getSerializableExtra("person") != null) {
            personEdit = (Person) intent.getSerializableExtra("person");
            etName.setText(personEdit.getName());
            etSurname.setText(personEdit.getSurname());

            if(personEdit.getAge() != null)
                etAge.setText(personEdit.getAge().toString());
        }

        editMode = intent.getBooleanExtra("editMode", false);
        if (!editMode)
            btnDelete.setVisibility(View.GONE);
        else
            btnDelete.setVisibility(View.VISIBLE);
    }

}