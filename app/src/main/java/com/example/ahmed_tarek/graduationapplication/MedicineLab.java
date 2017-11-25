package com.example.ahmed_tarek.graduationapplication;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Ahmed_Tarek on 17/11/21.
 */

public class MedicineLab {

    private char[] letters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    private static MedicineLab sMedicineLab;

    private List<Medicine> mMedicines;

    public static MedicineLab get() {
        if (sMedicineLab == null) {
            sMedicineLab = new MedicineLab();
        }
        return sMedicineLab;
    }

    private MedicineLab() {
        mMedicines = new ArrayList<>();
        for (int i = 0 ; i < 100 ; i++) {
            Medicine medicine = new Medicine();
            medicine.setName(letters[i % 26] + "Medicine #" + i);
            medicine.setForm("tab");
            medicine.setCategory("anti-pyretic");
            medicine.setConcentration(500);
            medicine.setActiveIngredients("paracetamol");
            medicine.setPrice(3.75 + (i * 2));
            mMedicines.add(medicine);
        }
    }


    public List<Medicine> getMedicines(CharSequence charSequence) {

        if (charSequence == null) {
            return null;
        } else {

            List<Medicine> medicineList = new ArrayList<>();
            for (Medicine medicine : mMedicines) {
                if (medicine.getName().startsWith(charSequence.toString())) {    // || medicine.getName().contains(charSequence)
                    medicineList.add(medicine);
                }
            }
            return medicineList;
        }
    }

    public Medicine getMedicine(UUID id) {
        for (Medicine medicine : mMedicines) {
            if (medicine.getID().equals(id)) {
                return medicine;
            }
        }
        return null;
    }
}
