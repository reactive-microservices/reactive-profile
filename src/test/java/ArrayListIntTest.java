import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static junit.framework.TestCase.assertEquals;


public class ArrayListIntTest {

    @Test
    public void addLotsOfRandomValues() {

        final int listSize = 32_000_000;

        List<Integer> list = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < listSize; ++i) {
            list.add(rand.nextInt());
        }

        assertEquals(listSize, list.size());
    }
}
