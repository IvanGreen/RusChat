import org.apache.commons.lang3.ArrayUtils;

public class Method {

    public int[] methodOne(int array[]){

        int newArray[] = ArrayUtils.subarray(array,
                ArrayUtils.lastIndexOf(array,4) + 1,
                ArrayUtils.getLength(array) + 1);
        if (array.equals(newArray)) throw new RuntimeException();
        return newArray;
    }


    public boolean methodTwo(int array[]){
        return (ArrayUtils.contains(array,1) || ArrayUtils.contains(array,4));
    }
}
