package es.iescarrillo.android.myfirstappfirebase.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.iescarrillo.android.myfirstappfirebase.R;
import es.iescarrillo.android.myfirstappfirebase.models.Person;
import es.iescarrillo.android.myfirstappfirebase.services.PersonService;

public class InsertOrEditPersonActivity extends AppCompatActivity {

    // Componentes de la pantalla
    private Button btnSave, btnCancel, btnDelete;
    private EditText etName, etSurname, etAge, etEmail, etPassword;

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
                    // Creamos la nueva persona
                    Person person = new Person();

                    if (etName.getText().toString().isBlank()) {
                        Toast.makeText(InsertOrEditPersonActivity.this, "Name is blank", Toast.LENGTH_SHORT).show();
                        return; // Para salir del listener
                    }
                    if (etSurname.getText().toString().isBlank()) {
                        Toast.makeText(InsertOrEditPersonActivity.this, "Surname is blank", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (etEmail.getText().toString().isBlank()) {
                        Toast.makeText(InsertOrEditPersonActivity.this, "Email is blank", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (etPassword.getText().toString().isBlank()) {
                        Toast.makeText(InsertOrEditPersonActivity.this, "Password is blank", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!etAge.getText().toString().isBlank())
                        person.setAge(Integer.valueOf(etAge.getText().toString()));

                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                            .addOnCompleteListener(InsertOrEditPersonActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseUser user = mAuth.getCurrentUser();

                                // Seteamos los valores con los introducidos en la pantalla y los que devuelve la autenticación
                                person.setName(etName.getText().toString());
                                person.setSurname(etSurname.getText().toString());
                                person.setEmail(user.getEmail());
                                person.setUid(user.getUid());
                                person.setProvider(user.getProviderId());

                                String idPerson = personService.insert(person);
                                Toast.makeText(InsertOrEditPersonActivity.this, "Person with id " + idPerson + " inserted", Toast.LENGTH_SHORT).show();
                                Log.i("Person id", idPerson);

                                Intent intent = new Intent(InsertOrEditPersonActivity.this, MainActivity.class);
                                startActivity(intent);

                            } else {
                                Toast.makeText(InsertOrEditPersonActivity.this, "Registration failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.getCurrentUser().delete().addOnCompleteListener(InsertOrEditPersonActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            personService.delete(personEdit);
                            Toast.makeText(getApplicationContext(), "Successfully removed", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(InsertOrEditPersonActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else{
                            Toast.makeText(getApplicationContext(), "An error has occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

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