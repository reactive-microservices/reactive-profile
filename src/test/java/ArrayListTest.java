import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ArrayListTest {


    @Test
    public void testList() {

        List<Integer> list = mock(LinkedList.class);
        list.add(1);
        list.add(2);
        list.add(3);

        verify(list).add(1);
        verify(list).add(2);
        verify(list).add(3);
    }
}
