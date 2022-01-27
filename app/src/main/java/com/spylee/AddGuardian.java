package com.spylee;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.spylee.model.Guardian;


public class AddGuardian extends Fragment {
    EditText name,email,phone,relation;
    Button add;
    DatabaseReference databaseReference;
    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;
    Guardian guardian;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_guardian, container, false);
        databaseReference = DbRef.getDbRef().child("guardians");
        sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        name = (EditText)view.findViewById(R.id.name);
        email = (EditText)view.findViewById(R.id.email);
        phone = (EditText)view.findViewById(R.id.phone);
        relation = (EditText)view.findViewById(R.id.relation);
        add = (Button) view.findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                guardian = new Guardian();
                guardian.setId(databaseReference.push().getKey());
                guardian.setName(name.getText().toString().trim());
                guardian.setEmail(email.getText().toString().trim());
                guardian.setPhone(phone.getText().toString().trim());
                guardian.setRelation(relation.getText().toString().trim());
                guardian.setPersonId(sharedpreferences.getString("id",null));
                guardian.setPersonId_phone(guardian.getPersonId()+"_"+guardian.getPhone());
                guardian.setPersonId_email(guardian.getPersonId()+"_"+guardian.getEmail());
                if(TextUtils.isEmpty(guardian.getName()) || TextUtils.isEmpty(guardian.getEmail()) || TextUtils.isEmpty(guardian.getPhone()) || TextUtils.isEmpty(guardian.getRelation())) {
                    Toast.makeText(getContext(), "Please Fill All Details", Toast.LENGTH_LONG).show();
                } else {
                    databaseReference.orderByChild("personId_phone").equalTo(guardian.getPersonId_phone()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int count = 0;
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                count++;
                            }
                            if (count > 0) {
                                Toast.makeText(getContext(), "Duplicate Guardian Phone Number", Toast.LENGTH_LONG).show();
                            } else {
                                databaseReference.orderByChild("personId_email").equalTo(guardian.getPersonId_email()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        int count2 = 0;
                                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                            count2++;
                                        }
                                        if (count2 > 0) {
                                            Toast.makeText(getContext(), "Duplicate Guardian Email", Toast.LENGTH_LONG).show();
                                        } else {
                                            databaseReference.child(guardian.getId()).setValue(guardian).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getContext(), "Guardian Added Successfully", Toast.LENGTH_LONG).show();
                                                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                                    fragmentManager.beginTransaction().replace(R.id.user_container, new UserHome()).commit();


                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
        return view;
    }

}
