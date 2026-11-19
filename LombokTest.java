import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LombokTest {
    private String name;

    public static void main(String[] args) {
        LombokTest test = new LombokTest();
        test.setName("FlowDesk");
        System.out.println(test.getName());
    }
}
