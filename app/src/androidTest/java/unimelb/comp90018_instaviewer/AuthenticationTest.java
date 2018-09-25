package unimelb.comp90018_instaviewer;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import unimelb.comp90018_instaviewer.utilities.Authentication;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class AuthenticationTest {
    @Test
    public void zeroTest(){
        Authentication test = new Authentication();
        test.run();
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("unimelb.comp90018_instaviewer", appContext.getPackageName());
    }

}
