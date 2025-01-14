package es.iescarrillo.android.myfirstappfirebase.activities;

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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import es.iescarrillo.android.myfirstappfirebase.R;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin, btnSignUp, btnSignInGoogle;
    private EditText etEmail, etPassword;
    private GoogleSignInOptions googleSignInOptions;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth instanceAuth;

    private static final int REQ_SIGN_IN = 2;

    @Override
    protected void onStart() {
        super.onStart();

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadComponents();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etEmail.getText().toString().isBlank()){
                    Toast.makeText(LoginActivity.this, R.string.email_blank, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (etPassword.getText().toString().isBlank()){
                    Toast.makeText(LoginActivity.this, R.string.password_blank, Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signInWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), R.string.login_successfull, Toast.LENGTH_SHORT);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.invalid_email_or_password, Toast.LENGTH_SHORT);
                        }
                    }
                });
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, InsertOrEditPersonActivity.class);
                // Indicamos que NO estamos en modo edición
                intent.putExtra("editMode", false);
                startActivity(intent);
                finish();
            }
        });

        btnSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                // Este método llama al intent anteriormente configurado, una vez finalizado
                // ejecutará el método onActivityResult.
                // REQ_SIGN_IN es un código de solicitud único que identifica esta operación.
                startActivityForResult(signInIntent, REQ_SIGN_IN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Llamada al constructor de la clase padre
        super.onActivityResult(requestCode, resultCode, data);

        // Verificamos si el resultado pertenece a la solicitud de inicio de sesión de Google
        if (requestCode == REQ_SIGN_IN) {
            try {
                // Obtiene la cuenta de Google seleccionada del Intent recibido
                // Si hubo un error, se lanza una excepción de tipo ApiException
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);

                // Crea una credencial de autenticación de Firebase usando el token de ID de Google
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                // Inicia sesión en Firebase con la credencial obtenida
                instanceAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, task -> {

                            // Si el inicio de sesión es exitoso, obtiene el usuario autenticado de Firebase Auth
                            if (task.isSuccessful()) {
                                FirebaseUser user = instanceAuth.getCurrentUser();

                                // Comprobamos si existe una persona en nuestro módulo de RealTime con el UID
                                // En caso de que exista, mostraremos la pantalla principal MainActivity
                                // En caso de que no exista, mostraremos la pantalla de registro con los datos precargados
                                FirebaseDatabase.getInstance().getReference("persons").orderByChild("uid").equalTo(user.getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                // Existe al menos una persona con ese UID
                                                if (snapshot.exists()){
                                                    Toast.makeText(LoginActivity.this, R.string.login_successfull, Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {// No existe ninguna persona con ese UID
                                                    Intent intent = new Intent(LoginActivity.this, InsertOrEditPersonActivity.class);
                                                    intent.putExtra("editMode", false);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(LoginActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            } else {
                                Toast.makeText(LoginActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (ApiException e) {
                Toast.makeText(LoginActivity.this, R.string.error_google_sign_in, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadComponents(){
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignInGoogle = findViewById(R.id.btnGoogle);

        etEmail = findViewById(R.id.etEmailLogin);
        etPassword = findViewById(R.id.etPasswordLogin);

        // Initialize Firebase Auth
        instanceAuth = FirebaseAuth.getInstance();

        // Configuración de Google Sign-In Options
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // Solicita un ID Token para autenticar al usuario
                .requestIdToken(getString(R.string.default_web_client_id)) // Se obtiene en la consola de Firebase
                .requestEmail() // Solicita acceso al correo electrónico del usuario
                .requestProfile() // Solicita acceso al perfil del usuario (nombre, foto, etc.)
                .build(); // Construye la configuración con las opciones especificadas

        // Inicializa el cliente de Google SignIn para mostrar la pantalla para elegir una cuenta de Google
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

    }
}