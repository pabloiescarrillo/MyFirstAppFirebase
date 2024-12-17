package es.iescarrillo.android.myfirstappfirebase.services;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import es.iescarrillo.android.myfirstappfirebase.models.Person;

// Clase que proporciona servicios para insertar, actualizar y eliminar personas en Firebase
public class PersonService {

    // Referencia a la base de datos en Firebase
    private DatabaseReference databaseReference;

    /**
     * Constructor de la clase PersonService.
     * Inicializa la referencia de la base de datos y apunta al nodo "persons".
     *
     * @param context Contexto de la aplicación (se podría usar si fuera necesario en otro caso).
     */
    public PersonService(Context context){
        // Inicializa la referencia apuntando al nodo "persons" en Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("persons");
    }

    /**
     * Inserta un objeto Person en la base de datos Firebase.
     *
     * @param person Objeto Person que se quiere insertar en la base de datos.
     * @return String Retorna el ID autogenerado de la persona insertada.
     */
    public String insert(Person person){

        /*
         * Generamos una nueva referencia en el nodo "persons".
         * El método push() crea una clave única y evita sobrescribir datos existentes.
         */
        DatabaseReference newReference = databaseReference.push();

        // Asignamos el ID único generado automáticamente por Firebase al objeto Person.
        person.setId(newReference.getKey());

        /*
         * Insertamos el objeto Person en la base de datos.
         * El método setValue() envía el objeto en formato JSON al nodo correspondiente.
         */
        newReference.setValue(person);

        // Retornamos el ID autogenerado del objeto Person insertado.
        return person.getId();
    }

    /**
     * Actualiza un objeto Person en la base de datos Firebase.
     *
     * @param person Objeto Person que contiene los datos actualizados.
     */
    public void update(Person person){
        /*
         * Accedemos al nodo específico utilizando el ID de la persona
         * y actualizamos su información usando el método setValue().
         */
        databaseReference.child(person.getId()).setValue(person);
    }

    /**
     * Elimina un objeto Person de la base de datos Firebase.
     *
     * @param person Objeto Person que se desea eliminar.
     */
    public void delete(Person person){
        /*
         * Accedemos al nodo específico usando el ID del objeto Person
         * y lo eliminamos con el método removeValue().
         */
        databaseReference.child(person.getId()).removeValue();
    }
}
