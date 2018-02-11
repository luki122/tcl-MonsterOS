package com.mst.thememanager.utils;

public class ArrayUtils {

	
	 /**
    * <p>Checks if an array of Objects is empty or {@code null}.</p>
    *
    * @param array  the array to test
    * @return {@code true} if the array is empty or {@code null}
    * @since 2.1
    */
   public static boolean isEmpty(Object[] array) {
       return array == null || array.length == 0;
   }

   /**
    * <p>Checks if an array of primitive longs is empty or {@code null}.</p>
    *
    * @param array  the array to test
    * @return {@code true} if the array is empty or {@code null}
    * @since 2.1
    */
   public static boolean isEmpty(long[] array) {
       return array == null || array.length == 0;
   }

   /**
    * <p>Checks if an array of primitive ints is empty or {@code null}.</p>
    *
    * @param array  the array to test
    * @return {@code true} if the array is empty or {@code null}
    * @since 2.1
    */
   public static boolean isEmpty(int[] array) {
       return array == null || array.length == 0;
   }

   /**
    * <p>Checks if an array of primitive shorts is empty or {@code null}.</p>
    *
    * @param array  the array to test
    * @return {@code true} if the array is empty or {@code null}
    * @since 2.1
    */
   public static boolean isEmpty(short[] array) {
       return array == null || array.length == 0;
   }

   /**
    * <p>Checks if an array of primitive chars is empty or {@code null}.</p>
    *
    * @param array  the array to test
    * @return {@code true} if the array is empty or {@code null}
    * @since 2.1
    */
   public static boolean isEmpty(char[] array) {
       return array == null || array.length == 0;
   }

   /**
    * <p>Checks if an array of primitive bytes is empty or {@code null}.</p>
    *
    * @param array  the array to test
    * @return {@code true} if the array is empty or {@code null}
    * @since 2.1
    */
   public static boolean isEmpty(byte[] array) {
       return array == null || array.length == 0;
   }

   /**
    * <p>Checks if an array of primitive doubles is empty or {@code null}.</p>
    *
    * @param array  the array to test
    * @return {@code true} if the array is empty or {@code null}
    * @since 2.1
    */
   public static boolean isEmpty(double[] array) {
       return array == null || array.length == 0;
   }

   /**
    * <p>Checks if an array of primitive floats is empty or {@code null}.</p>
    *
    * @param array  the array to test
    * @return {@code true} if the array is empty or {@code null}
    * @since 2.1
    */
   public static boolean isEmpty(float[] array) {
       return array == null || array.length == 0;
   }

   /**
    * <p>Checks if an array of primitive booleans is empty or {@code null}.</p>
    *
    * @param array  the array to test
    * @return {@code true} if the array is empty or {@code null}
    * @since 2.1
    */
   public static boolean isEmpty(boolean[] array) {
       return array == null || array.length == 0;
   }

   // ----------------------------------------------------------------------
   /**
    * <p>Checks if an array of Objects is not empty or not {@code null}.</p>
    *
    * @param <T> the component type of the array
    * @param array  the array to test
    * @return {@code true} if the array is not empty or not {@code null}
    * @since 2.5
    */
    public static <T> boolean isNotEmpty(T[] array) {
        return (array != null && array.length != 0);
    }

   /**
    * <p>Checks if an array of primitive longs is not empty or not {@code null}.</p>
    *
    * @param array  the array to test
    * @return {@code true} if the array is not empty or not {@code null}
    * @since 2.5
    */
   public static boolean isNotEmpty(long[] array) {
       return (array != null && array.length != 0);
   }

   /**
    * <p>Checks if an array of primitive ints is not empty or not {@code null}.</p>
    *
    * @param array  the array to test
    * @return {@code true} if the array is not empty or not {@code null}
    * @since 2.5
    */
   public static boolean isNotEmpty(int[] array) {
       return (array != null && array.length != 0);
   }

   /**
    * <p>Checks if an array of primitive shorts is not empty or not {@code null}.</p>
    *
    * @param array  the array to test
    * @return {@code true} if the array is not empty or not {@code null}
    * @since 2.5
    */
   public static boolean isNotEmpty(short[] array) {
       return (array != null && array.length != 0);
   }

   /**
    * <p>Checks if an array of primitive chars is not empty or not {@code null}.</p>
    *
    * @param array  the array to test
    * @return {@code true} if the array is not empty or not {@code null}
    * @since 2.5
    */
   public static boolean isNotEmpty(char[] array) {
       return (array != null && array.length != 0);
   }

   /**
    * <p>Checks if an array of primitive bytes is not empty or not {@code null}.</p>
    *
    * @param array  the array to test
    * @return {@code true} if the array is not empty or not {@code null}
    * @since 2.5
    */
   public static boolean isNotEmpty(byte[] array) {
       return (array != null && array.length != 0);
   }

   /**
    * <p>Checks if an array of primitive doubles is not empty or not {@code null}.</p>
    *
    * @param array  the array to test
    * @return {@code true} if the array is not empty or not {@code null}
    * @since 2.5
    */
   public static boolean isNotEmpty(double[] array) {
       return (array != null && array.length != 0);
   }

   /**
    * <p>Checks if an array of primitive floats is not empty or not {@code null}.</p>
    *
    * @param array  the array to test
    * @return {@code true} if the array is not empty or not {@code null}
    * @since 2.5
    */
   public static boolean isNotEmpty(float[] array) {
       return (array != null && array.length != 0);
   }

   /**
    * <p>Checks if an array of primitive booleans is not empty or not {@code null}.</p>
    *
    * @param array  the array to test
    * @return {@code true} if the array is not empty or not {@code null}
    * @since 2.5
    */
   public static boolean isNotEmpty(boolean[] array) {
       return (array != null && array.length != 0);
   }
	
   
   /**
    * <p>Reverses the order of the given array.</p>
    *
    * <p>There is no special handling for multi-dimensional arrays.</p>
    *
    * <p>This method does nothing for a {@code null} input array.</p>
    *
    * @param array  the array to reverse, may be {@code null}
    */
   public static void reverse(Object[] array) {
       if (array == null) {
           return;
       }
       int i = 0;
       int j = array.length - 1;
       Object tmp;
       while (j > i) {
           tmp = array[j];
           array[j] = array[i];
           array[i] = tmp;
           j--;
           i++;
       }
   }

   /**
    * <p>Reverses the order of the given array.</p>
    *
    * <p>This method does nothing for a {@code null} input array.</p>
    *
    * @param array  the array to reverse, may be {@code null}
    */
   public static void reverse(long[] array) {
       if (array == null) {
           return;
       }
       int i = 0;
       int j = array.length - 1;
       long tmp;
       while (j > i) {
           tmp = array[j];
           array[j] = array[i];
           array[i] = tmp;
           j--;
           i++;
       }
   }

   /**
    * <p>Reverses the order of the given array.</p>
    *
    * <p>This method does nothing for a {@code null} input array.</p>
    *
    * @param array  the array to reverse, may be {@code null}
    */
   public static void reverse(int[] array) {
       if (array == null) {
           return;
       }
       int i = 0;
       int j = array.length - 1;
       int tmp;
       while (j > i) {
           tmp = array[j];
           array[j] = array[i];
           array[i] = tmp;
           j--;
           i++;
       }
   }

   /**
    * <p>Reverses the order of the given array.</p>
    *
    * <p>This method does nothing for a {@code null} input array.</p>
    *
    * @param array  the array to reverse, may be {@code null}
    */
   public static void reverse(short[] array) {
       if (array == null) {
           return;
       }
       int i = 0;
       int j = array.length - 1;
       short tmp;
       while (j > i) {
           tmp = array[j];
           array[j] = array[i];
           array[i] = tmp;
           j--;
           i++;
       }
   }

   /**
    * <p>Reverses the order of the given array.</p>
    *
    * <p>This method does nothing for a {@code null} input array.</p>
    *
    * @param array  the array to reverse, may be {@code null}
    */
   public static void reverse(char[] array) {
       if (array == null) {
           return;
       }
       int i = 0;
       int j = array.length - 1;
       char tmp;
       while (j > i) {
           tmp = array[j];
           array[j] = array[i];
           array[i] = tmp;
           j--;
           i++;
       }
   }

   /**
    * <p>Reverses the order of the given array.</p>
    *
    * <p>This method does nothing for a {@code null} input array.</p>
    *
    * @param array  the array to reverse, may be {@code null}
    */
   public static void reverse(byte[] array) {
       if (array == null) {
           return;
       }
       int i = 0;
       int j = array.length - 1;
       byte tmp;
       while (j > i) {
           tmp = array[j];
           array[j] = array[i];
           array[i] = tmp;
           j--;
           i++;
       }
   }

   /**
    * <p>Reverses the order of the given array.</p>
    *
    * <p>This method does nothing for a {@code null} input array.</p>
    *
    * @param array  the array to reverse, may be {@code null}
    */
   public static void reverse(double[] array) {
       if (array == null) {
           return;
       }
       int i = 0;
       int j = array.length - 1;
       double tmp;
       while (j > i) {
           tmp = array[j];
           array[j] = array[i];
           array[i] = tmp;
           j--;
           i++;
       }
   }

   /**
    * <p>Reverses the order of the given array.</p>
    *
    * <p>This method does nothing for a {@code null} input array.</p>
    *
    * @param array  the array to reverse, may be {@code null}
    */
   public static void reverse(float[] array) {
       if (array == null) {
           return;
       }
       int i = 0;
       int j = array.length - 1;
       float tmp;
       while (j > i) {
           tmp = array[j];
           array[j] = array[i];
           array[i] = tmp;
           j--;
           i++;
       }
   }

   /**
    * <p>Reverses the order of the given array.</p>
    *
    * <p>This method does nothing for a {@code null} input array.</p>
    *
    * @param array  the array to reverse, may be {@code null}
    */
   public static void reverse(boolean[] array) {
       if (array == null) {
           return;
       }
       int i = 0;
       int j = array.length - 1;
       boolean tmp;
       while (j > i) {
           tmp = array[j];
           array[j] = array[i];
           array[i] = tmp;
           j--;
           i++;
       }
   }
   
}
