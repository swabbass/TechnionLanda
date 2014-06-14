package utils;

public enum Role {

    TUTOR, ACADIMIC_CORDINATOR, SOCIAL_CORDINATOR;

    public static Role getRole(int pos) {
        Role r = TUTOR;
        switch (pos) {
            case 1:
                r = TUTOR;
                break;
            case 3:
                r = ACADIMIC_CORDINATOR;
                break;
            case 4:
                r = SOCIAL_CORDINATOR;
                break;
        }
        return r;
    }
}
