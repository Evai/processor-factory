# processor-factory
简单的处理器工厂。可以在编译期间对枚举类进行解析并且生成一个对应的常量类。
### enum demo
```java

@MappingConstant(withEnumName = true)
public enum  UserEnum implements IErrorCode {

    /**
     * 错误码
     */
    PASSWORD_ERROR(1002, "密码错误, 用户名: {0}"),

    USERNAME_ERROR(1001, "用户名错误, 用户名: {0}"),

    USER_NOT_EXIST(1000, "用户不存在, 用户名: {0}");

    private final int code;

    private final String msg;


    UserEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getMsg() {
        return this.msg;
    }

    @Override
    public int getCode() {
        return this.code;
    }
}

```

### constant at java compile
```java

public final class UserEnumConstant {
  private UserEnumConstant() {
  }

  public static final class PasswordError {
    public static final String NAME = "PASSWORD_ERROR";

    public static final int CODE = 1002;

    public static final String MSG = "密码错误, 用户名: {0}";

    private PasswordError() {
    }
  }

  public static final class UsernameError {
    public static final String NAME = "USERNAME_ERROR";

    public static final int CODE = 1001;

    public static final String MSG = "用户名错误, 用户名: {0}";

    private UsernameError() {
    }
  }

  public static final class UserNotExist {
    public static final String NAME = "USER_NOT_EXIST";

    public static final int CODE = 1000;

    public static final String MSG = "用户不存在, 用户名: {0}";

    private UserNotExist() {
    }
  }
}

```

