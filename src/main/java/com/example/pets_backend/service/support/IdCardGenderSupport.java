package com.example.pets_backend.service.support;

/**
 * 根据中国大陆 18 位身份证号末位推断性别。
 * 规则：末位为奇数 → 男(1)；末位为偶数或 X → 女(2)。
 */
public final class IdCardGenderSupport {

    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;

    private IdCardGenderSupport() {
    }

    /**
     * @return 1=男，2=女；无法解析时返回 null
     */
    public static Integer resolveGender(String idCardNo) {
        if (idCardNo == null || idCardNo.length() != 18) {
            return null;
        }
        char last = Character.toUpperCase(idCardNo.charAt(17));
        if (last == 'X') {
            return GENDER_FEMALE;
        }
        if (last >= '0' && last <= '9') {
            int digit = last - '0';
            return digit % 2 == 0 ? GENDER_FEMALE : GENDER_MALE;
        }
        return null;
    }
}
