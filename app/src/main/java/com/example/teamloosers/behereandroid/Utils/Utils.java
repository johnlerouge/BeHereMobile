package com.example.teamloosers.behereandroid.Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Enseignant;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Structures.Identifiable;
import com.example.teamloosers.behereandroid.Structures.Personne;
import com.example.teamloosers.behereandroid.Structures.Ref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * Created by redjohn on 16/04/17.
 */

public class Utils {

    public static final String DATABASE_ERR_MESSAGE = "Une érreur s'est produit lors de la connexion" +
            "à la base de donnée";
    public static FirebaseDatabase database;

    public static Enseignant enseignant;
    static {

        /*enseignant = new Enseignant("Medles", "Mourad", Personne.FEMME);
        enseignant.setId("095d24da-6529-4cba-a7a6-1f3d4db1024a");
        enseignant.setEmail("safsaf.abderraouf@gmail.com");*/

        database = FirebaseDatabase.getInstance();

        // Enable disk persistence (Offline)
        //database.setPersistenceEnabled(true);
    }

    public final static String  CYCLES = "/Cycles", SPECIALITE_PROMOS = "/Specialite_Promos",
            FILLIERE_SPECIALITES = "/Filliere_Specialites",
            MODULE_ENSEIGNANTS = "/Module_Enseignants",
            ENSEIGNANT_MODULE = "/Enseignant_Modules",
            PROMO_MODULES = "/Promo_Modules",
            GROUPES = "/Groupes", SECTIONS = "/Sections",
            PHOTO_URL_ATTRIBUT = "imageBase64",
            SCORE = "/score";

    public static String firebasePath(String ... noeuds)   {

        String path = "";
        for (int i = 0; i < noeuds.length; i++) {

            path += String.format("%s/", noeuds[i]);
        }
        return path;
    }
    public static <P extends Identifiable, C extends Identifiable> ArrayList<C>
    getChildrenFromDataSnapshot(DataSnapshot dataSnapshot, Class<P> parentClass, Class<C> childClass) {

        P parentObject = null; C childObject = null;

        ArrayList<DataSnapshot> dataList = getListFromIterable(dataSnapshot.getChildren());
        try {

            if (!Modifier.isAbstract(parentClass.getModifiers()))
                parentObject = parentClass.newInstance();
            childObject = childClass.newInstance();
        } catch (Exception e)   {   }
        if (!Modifier.isAbstract(parentClass.getModifiers()))
            deleteAttributsFromArrayList(dataList, parentObject.getMap());

        ArrayList<C> childrenList = new ArrayList<>();

        C obj = null;
        for (DataSnapshot dataSnap: dataList)   {

            HashMap<String, Object> attributs = getOnlyAttributs(dataSnap,
                    childObject.getMap());
            try {
                obj = (C) childClass.newInstance();
            } catch (Exception e)   {   }

            obj.setAttributs(attributs);
            childrenList.add(obj);
        }
        return childrenList;
    }
    private static <E> ArrayList<E> getListFromIterable(Iterable<E> iter) {

        ArrayList<E> list = new ArrayList<E>();
        for (E item : iter) {

            list.add(item);
        }
        return list;
    }
    private static void deleteAttributsFromArrayList(ArrayList<DataSnapshot> dataList,
                                                     Map<String, Object> attributsMap) {

        Iterator<DataSnapshot> snapshotIterator= dataList.iterator();

        while (snapshotIterator.hasNext())  {

            DataSnapshot element = snapshotIterator.next();
            if (attributsMap.containsKey(element.getKey()))
                snapshotIterator.remove();
        }
    }
    private static HashMap<String, Object> getOnlyAttributs(DataSnapshot snapshot,
                                                            Map<String, Object> atrributsMap)    {

        HashMap<String, Object> attributsHashMap = new HashMap<>();


        for (DataSnapshot attribut: snapshot.getChildren())    {

            if(atrributsMap.containsKey(attribut.getKey()))
                attributsHashMap.put(attribut.getKey(), attribut.getValue());
        }
        return attributsHashMap;
    }
    public static String generateId()   {

        return UUID.randomUUID().toString();
    }
    public static String generateId(Ref obj)   {

        String designation = obj.getDesignation();
        designation = designation.replaceAll("\\s+","");

        String id = String.format("%s_%s", designation, UUID.randomUUID().toString());
        return id;
    }

    public static Bitmap decodeToImage(String imageString) {

        byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedByte;
    }
    public static void showSnackBar(Activity activity, String message) {

        View rootView = activity.getWindow().getDecorView().getRootView();
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        TextView snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextColor(ContextCompat.getColor(rootView.getContext(), R.color.white));
        snackbar.show();
    }
    public static void envoyerNotification(final Activity activity, final Etudiant etudiant, final String message)   {

        final FirebaseRemoteConfig mRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings remoteConfigSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();
        mRemoteConfig.setConfigSettings(remoteConfigSettings);

        long cacheExpiration = 3600;
        if (mRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            // task successful. Activate the fetched data
                            mRemoteConfig.activateFetched();

                            final String email = mRemoteConfig.getString("email");
                            final String password = mRemoteConfig.getString("password");

                            Thread thread = new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    try  {
                                        Mail m = new Mail(email, password);

                                        String[] toArr = {etudiant.getEmail()};
                                        m.set_to(toArr);
                                        m.set_from(email);
                                        m.set_subject(activity.getString(R.string.email_subject));
                                        m.setBody(message);
                                        try {
                                            if (m.send())
                                                System.out.println("Email envoye");
                                            else System.out.println("Erreur envoie email");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            thread.start();
                        } else {
                            //task failed
                        }
                    }
                });
    }
    public static void setActivityFullScreen(Activity activity) {

        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    public static void setRecyclerViewDecoration(RecyclerView recyclerView) {

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                LinearLayout.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.recyclerview_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
    }
    public static void setActionBarTitle(AppCompatActivity activity, String title) {

        activity.getSupportActionBar().setTitle(title);
    }
    public static void setActionBarSubtitle(AppCompatActivity activity, String title) {

        activity.getSupportActionBar().setSubtitle(title);
    }
}
