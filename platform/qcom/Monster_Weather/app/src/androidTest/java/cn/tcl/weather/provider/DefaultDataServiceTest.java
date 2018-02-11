/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.provider;

/**
 * Created by thundersoft on 16-7-28.
 */
public class DefaultDataServiceTest extends BaseTestCase {
    private final static int test_size = 10;
    private int itemCount = 0;

    public void testServiceRunning() {
        for (int i = 0; i < test_size; i++) {
            mDefaultService.requestData(new TestParam(i));
        }
        lock.lockWait(20000);
        assertEquals(test_size, itemCount);
    }


    private class TestParam extends AbsProviderDataParam {

        private int index;

        TestParam(int i) {
            index = i;
        }

        @Override
        protected void onDataScan(ProviderDataService service) {
            assertEquals("item is processed in order of serial", index, itemCount++);
            if (itemCount == test_size) {
                lock.lockNotify();
            }
        }
    }
}
