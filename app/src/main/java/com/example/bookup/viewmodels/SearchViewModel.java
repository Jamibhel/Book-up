package com.example.bookup.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookup.models.StudyMaterial;
import com.example.bookup.models.Tutor;
import com.example.bookup.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchViewModel extends ViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    
    private final MutableLiveData<List<StudyMaterial>> filteredMaterials = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Tutor>> filteredTutors = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Tutor>> filteredStudents = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private List<StudyMaterial> allMaterials = new ArrayList<>();
    private List<Tutor> allTutors = new ArrayList<>();
    private List<Tutor> allStudents = new ArrayList<>();

    public LiveData<List<StudyMaterial>> getFilteredMaterials() { return filteredMaterials; }
    public LiveData<List<Tutor>> getFilteredTutors() { return filteredTutors; }
    public LiveData<List<Tutor>> getFilteredStudents() { return filteredStudents; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void loadInitialData() {
        isLoading.setValue(true);
        
        // Fetch Materials
        db.collection("studyMaterials").get().addOnSuccessListener(snapshots -> {
            allMaterials.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                StudyMaterial m = doc.toObject(StudyMaterial.class);
                m.setId(doc.getId());
                allMaterials.add(m);
            }
            filteredMaterials.setValue(new ArrayList<>(allMaterials));
            checkLoadingComplete();
        });

        // Fetch All Users
        db.collection("users").get().addOnSuccessListener(snapshots -> {
            allTutors.clear();
            allStudents.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                User user = doc.toObject(User.class);
                Tutor t = mapUserToTutor(doc.getId(), user);
                
                if ("tutor".equalsIgnoreCase(user.getRole())) {
                    allTutors.add(t);
                } else {
                    allStudents.add(t);
                }
            }
            filteredTutors.setValue(new ArrayList<>(allTutors));
            filteredStudents.setValue(new ArrayList<>(allStudents));
            checkLoadingComplete();
        });
    }

    public void filter(String query) {
        String lowerQuery = query.toLowerCase().trim();
        
        if (lowerQuery.isEmpty()) {
            filteredMaterials.setValue(new ArrayList<>(allMaterials));
            filteredTutors.setValue(new ArrayList<>(allTutors));
            filteredStudents.setValue(new ArrayList<>(allStudents));
            return;
        }

        // Filter Materials
        List<StudyMaterial> mList = new ArrayList<>();
        for (StudyMaterial m : allMaterials) {
            if (m.getTitle() != null && m.getTitle().toLowerCase().contains(lowerQuery)) {
                mList.add(m);
            }
        }
        filteredMaterials.setValue(mList);

        // Filter Tutors
        List<Tutor> tList = new ArrayList<>();
        for (Tutor t : allTutors) {
            if (t.getName() != null && t.getName().toLowerCase().contains(lowerQuery)) {
                tList.add(t);
            }
        }
        filteredTutors.setValue(tList);

        // Filter Students
        List<Tutor> sList = new ArrayList<>();
        for (Tutor s : allStudents) {
            if (s.getName() != null && s.getName().toLowerCase().contains(lowerQuery)) {
                sList.add(s);
            }
        }
        filteredStudents.setValue(sList);
    }

    private Tutor mapUserToTutor(String id, User user) {
        Tutor t = new Tutor();
        t.setUid(id);
        t.setName(user.getDisplayName());
        t.setProfileImageUrl(user.getPhotoUrl());
        t.setBio(user.getBio());
        t.setSubjects(user.getTutoringSubjects());
        t.setRating(user.getRating());
        t.setReviewCount(user.getReviewCount());
        t.setAvailable(user.isAvailable());
        return t;
    }

    private int fetchCount = 0;
    private synchronized void checkLoadingComplete() {
        fetchCount++;
        if (fetchCount >= 2) {
            isLoading.postValue(false);
        }
    }
}
