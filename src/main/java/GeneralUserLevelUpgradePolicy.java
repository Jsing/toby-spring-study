public class GeneralUserLevelUpgradePolicy implements UserLevelUpgradePolicy {
    private UserDao userDao;
    public static final int MIN_LOGIN_COUNT_FOR_SILVER = 50;
    public static final int MIN_RECOMMEND_COUNT_FOR_GOLD = 30;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean can(User user) {
        return switch (user.getLevel()) {
            case BASIC -> user.getLogin() >= MIN_LOGIN_COUNT_FOR_SILVER;
            case SILVER -> user.getRecommend() >= MIN_RECOMMEND_COUNT_FOR_GOLD;
            case GOLD -> false;
            // default -> throw new IllegalArgumentException("Unknown Level: " + currentLevel);
            // 위 default 문은 새로운 Switch 문에서 불필요합니다. Enum 값이 추가되었는데 case 가 추가되지 않으면 컴파일 오류가 발생합니다.
        };
    }

    @Override
    public void upgrade(User user) {
        user.upgradeLevel(); // User 객체의 레벨을 업그레이드 하고,
        userDao.update(user); // DB에 있는 User 정보를 업그레이드 합니다. 와 간결하다!!!
    }
}
