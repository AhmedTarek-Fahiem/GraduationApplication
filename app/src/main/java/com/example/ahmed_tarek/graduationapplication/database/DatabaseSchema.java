package com.example.ahmed_tarek.graduationapplication.database;

/**
 * Created by Ahmed_Tarek on 17/12/18.
 */

public class DatabaseSchema {

    public static final String DATABASE_NAME = "project.db";


    public class UserDbSchema {

        public final class UserTable {
            public final static String NAME = "user";

            public final class UserColumns {
                public static final String USER_UUID = "user_uuid";
                public static final String USER_NAME = "username";
                public static final String USER_PASSWORD = "user_password";
                public static final String USER_EMAIL = "user_email";
                public static final String USER_DATE_OF_BIRTH = "user_date_of_birth";
                public static final String USER_GENDER = "user_gender";
            }
        }
    }


    public class HistoryDbSchema {

        public final class HistoryTable {
            public final static String NAME = "history";

            public final class HistoryColumns {
                public static final String HISTORY_UUID = "history_uuid";
                public static final String USER_UUID = "user_uuid";
                public static final String DISEASE_HISTORY = "disease_history";
                public static final String CURRENT_MEDICAL_STATUS = "current_medical_status";
            }
        }
    }


    public class MedicineDbSchema {

        public final class MedicineTable {
            public static final String NAME = "medicine";

            public final class MedicineColumns {
                public static final String MEDICINE_UUID = "medicine_uuid";
                public static final String MEDICINE_NAME = "medicine_name";
                public static final String MEDICINE_CATEGORY = "medicine_category";
                public static final String MEDICINE_FORM = "medicine_form";
                public static final String MEDICINE_CONCENTRATION = "medicine_concentration";
                public static final String MEDICINE_ACTIVE_INGREDIENTS = "medicine_active_ingredients";
                public static final String MEDICINE_PRICE = "medicine_price";
                public static final String MEDICINE_QUANTITY = "medicine_quantity";
            }
        }
    }


    public class PrescriptionDbSchema {

        public final class PrescriptionTable {
            public static final String NAME = "prescription";

            public final class PrescriptionColumns {
                public static final String PRESCRIPTION_UUID = "prescription_uuid";
                public static final String PRESCRIPTION_DATE = "prescription_date";
                public static final String PRESCRIPTION_PRICE = "prescription_price";
                public static final String USER_UUID = "user_uuid";
                public static final String HISTORY_UUID = "history_uuid";
            }
        }
    }


    public class CartMedicineDbSchema {

        public final class CartMedicineTable {
            public static final String NAME = "cart_medicine";

            public final class CartMedicineColumns {
                public static final String MEDICINE_UUID = "medicine_uuid";
                public static final String PRESCRIPTION_UUID = "prescription_uuid";
                public static final String QUANTITY = "quantity";
                public static final String REPEAT_DURATION = "repeat_duration";
            }
        }
    }


    public class RegularOrderDbSchema {

        public final class RegularOrderTable {
            public static final String NAME = "regular_order";

            public final class RegularOrderColumns {
                public static final String USER_UUID = "user_uuid";
                public static final String PRESCRIPTION_UUID = "prescription_uuid";
                public static final String FIRE_TIME = "fire_time";
            }
        }
    }


}