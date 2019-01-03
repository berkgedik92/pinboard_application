package project.pinboard.Services;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
class ListOperations {

    //Return list1 - list2
    public List<String> findDifference(List<String> list1, List<String> list2) {
        List<String> result = new ArrayList<>();
        for (String element : list1)
            if (!list2.contains(element))
                result.add(element);
        return result;
    }
}
