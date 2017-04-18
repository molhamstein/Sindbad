package com.leenita.sindbad;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.viewanimator.ViewAnimator;

import com.facebook.Profile;
import com.leenita.sindbad.data.DataStore;
import com.leenita.sindbad.data.DataStore.App_ACCESS_MODE;
import com.leenita.sindbad.data.DataStore.DataRequestCallback;
import com.leenita.sindbad.data.FacebookProvider;
import com.leenita.sindbad.data.FacebookProviderListener;
import com.leenita.sindbad.data.ServerAccess;
import com.leenita.sindbad.data.ServerResult;
import com.leenita.sindbad.model.AppUser;

import java.util.HashMap;

public class LoginActivity extends BaseActivity implements OnClickListener {

    public static final int REQ_CODE_VERIFICATION = 456;
    public static final int REQ_CODE_COUNTRY_LIST_ACTIVITY = 356;
    public static final int REQ_CODE_CHOOSE_FAV_CATEGORIES = 256;

    public enum phoeNumCheckResult {OK, SHORT, WRONG, EMPTY}

    private enum LOGIN_STAGE {ENTER_PHONE_NUM, ENTER_USER_DETAILS}

    private boolean attemptingLogin = false;

    // Values for email and password at the time of the login attempt.
    String attemptingFName;
    String attemptingLName;
    String attemptingPhoneNum;
    String attemptingCountryCode;

    // UI references.
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;
    private View bgParallax;

    //private LinearLayout llPhoneNumForm;
    //ScrollView svUserInfoForm ;
    EditText etSignupFName, etSignupLName;
    View btnSignup, btnFBlogin, btnEnterPhoneNum;

    private LOGIN_STAGE currentLoginStage;
    private boolean linkWithFB = false; // indicates if we should try to link with facebook whenSignUp is done ;
    private String FbToken = null;

    Handler handler;

    Fragment currentFrag;
    FragmentManager fragMgr;


    FacebookProviderListener facebookLoginListner = new FacebookProviderListener() {

        @Override
        public void onFacebookSessionOpened(String accessToken, String userId) {
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);

            FbToken = accessToken;
            Profile profile = com.facebook.Profile.getCurrentProfile();
            String fname = profile.getFirstName();
            String lname = profile.getLastName();
            String id = profile.getId();

            DataStore.getInstance().attemptSignUp(attemptingPhoneNum, fname, lname, attemptingCountryCode, DataStore.VERSIOIN_ID, id, apiLoginCallback);
            linkWithFB = true;
            FacebookProvider.getInstance().unregisterListener();
        }

        @Override
        public void onFacebookSessionClosed() {
        }

        @Override
        public void onFacebookException(Exception exception) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        init();
        animateIn();
    }

    private void init() {

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
        bgParallax = findViewById(R.id.bgParallax);

        fragMgr = getSupportFragmentManager();
        currentLoginStage = LOGIN_STAGE.ENTER_PHONE_NUM;
        handler = new Handler();
        mLoginFormView.setVisibility(View.GONE);
        switchLoginStage(currentLoginStage);
        showProgress(false);
    }


    private void animateIn() {
        int bgScrollDistance = SindbadApp.getPXSize(900) - getResources().getDisplayMetrics().widthPixels;
        ViewAnimator.animate(bgParallax).translationX(-bgScrollDistance).duration(100000)
                .interpolator(new LinearInterpolator())
                .start();

    }

    void hideKeypoard(TextView tv) {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(tv.getWindowToken(), 0);
        } catch (Exception e) {
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //getMenuInflater().inflate(R.menu.activity_login, menu);
        return true;
    }

    /**
     * try login first using facebook if success then singning up to the API Server using the
     * facebook Id and phone number entered in the previous stage
     */
    public void attempFBtLogin() {

        if (attemptingPhoneNum == null || attemptingPhoneNum.isEmpty())
            return;

        resetInputErrors();

        //Session.openActiveSession(this, true, permissions, callback)
        FacebookProvider.getInstance().registerListener(facebookLoginListner);
        FacebookProvider.getInstance().requestFacebookLogin(this);
        //Session.StatusCallback callback =  new LoginStatsCallback() ;
        //Session.openActiveSession(LoginActivity.this, true, perm1, callback ) ;
        showProgress(true);
    }

    public void attempSignUp() {

        // Store values at the time of the login attempt.
        attemptingFName = etSignupFName.getText().toString();
        attemptingLName = etSignupLName.getText().toString();
        resetInputErrors();

        boolean cancel = false;
        View focusView = null;

        if (attemptingFName == null || attemptingFName.length() < 2) {
            etSignupFName.setError(getString(R.string.error_user_name_required));
            cancel = true;
            focusView = etSignupFName;
        } else if (attemptingLName == null || attemptingLName.length() < 2) {
            etSignupLName.setError(getString(R.string.error_user_name_required));
            cancel = true;
            focusView = etSignupLName;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            DataStore.getInstance().attemptSignUp(attemptingPhoneNum, attemptingFName, attemptingLName,attemptingCountryCode, DataStore.VERSIOIN_ID, "", apiLoginCallback);
        }
    }

    public void attempLogIn() {
        try {
            attemptingPhoneNum = ((FragPhoenNumberForm)currentFrag).getPhoneNumber().replaceAll("\\s+", "");
            attemptingCountryCode = ((FragPhoenNumberForm)currentFrag).getCountryCode();
            resetInputErrors();
            boolean cancel = false;
            View focusView = null;

            // Check for a valid number.
            phoeNumCheckResult numValid = validatePhoneNum(attemptingPhoneNum);

            switch (numValid) {
                case SHORT:
                    ((FragPhoenNumberForm)currentFrag).etPhoneNum.setError(getString(R.string.error_short_phone_num));
                    focusView = ((FragPhoenNumberForm)currentFrag).etPhoneNum;
                    cancel = true;
                    break;
                case EMPTY:
                    ((FragPhoenNumberForm)currentFrag).etPhoneNum.setError(getString(R.string.error_field_required));
                    focusView = ((FragPhoenNumberForm)currentFrag).etPhoneNum;
                    cancel = true;
                    break;
                case WRONG:
                    ((FragPhoenNumberForm)currentFrag).etPhoneNum.setError(getString(R.string.error_incorrect_phone_num));
                    focusView = ((FragPhoenNumberForm)currentFrag).etPhoneNum;
                    cancel = true;
                    break;
            }

            if (cancel) {
                focusView.requestFocus();
            } else {
                showProgress(true);
                DataStore.getInstance().attemptLogin(attemptingPhoneNum, apiLoginCallback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void resetInputErrors() {
        try {
            ((FragPhoenNumberForm)currentFrag).etPhoneNum.setError(null);
        } catch (Exception ignored) {
        }
        try {
            etSignupFName.setError(null);
            etSignupLName.setError(null);
        } catch (Exception ignored) {
        }
    }

    private void switchLoginStage(LOGIN_STAGE newStage) {
        try {
            switch (newStage) {
                case ENTER_PHONE_NUM:
                    currentFrag = new FragPhoenNumberForm();
                    fragMgr.beginTransaction()
                            //.setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right,R.anim.slide_in_from_right,R.anim.slide_out_to_left)
                            //.addToBackStack(section.name())
                            .replace(R.id.login_form, currentFrag)
                            .commit();
                    break;
                case ENTER_USER_DETAILS:
                    currentFrag = new FragUserDetailsForm();
                    fragMgr.beginTransaction()
                            //.setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right,R.anim.slide_in_from_right,R.anim.slide_out_to_left)
                            .addToBackStack(newStage.name())
                            .replace(R.id.login_form, currentFrag)
                            .commit();
                    break;
            }
            currentLoginStage = newStage;
            resetInputErrors();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FacebookProvider.getInstance().onActiviyResult(requestCode, resultCode, data);
        if(requestCode == REQ_CODE_CHOOSE_FAV_CATEGORIES && resultCode == RESULT_OK){
            setResult(RESULT_OK);
            finish();
        }
        //Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    protected void showChooseFavCategoriesActivity() {
        try {
            Intent intent = new Intent(this, ChooseFavCategoriesActivity.class);
            startActivityForResult(intent, LoginActivity.REQ_CODE_CHOOSE_FAV_CATEGORIES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    DataRequestCallback apiLoginCallback = new DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {
            try {
                attemptingLogin = false;
                showProgress(false);
                // success means data is retrived from server and does not indicate login success
                if (success) {
                    //boolean loginSuccess = (Boolean) data.get("loginResult");
                    //LoginMode loginMode = (LoginMode) data.get("loginMode");
                    if (result.getRequestStatusCode() == ServerAccess.ERROR_CODE_userNotExists) { // user tried to login but server didnt recognize him "he isn't registered before"
                        switchLoginStage(LOGIN_STAGE.ENTER_USER_DETAILS);
                    } else if (result.getRequestStatusCode() == ServerAccess.ERROR_CODE_userExistsBefore) {
                        //SindbadApp.displayCustomToast(getString(R.string.login_msg_facebook_account_already_exists));
                    } else {
                        HashMap<String, Object> data = result.getPairs();
                        AppUser user = (AppUser) data.get("appUser");
                        DataStore.getInstance().requestGCMRegsitrationId();
                        DataStore.getInstance().triggerDataUpdate();
                        DataStore.getInstance().setAccessMode(App_ACCESS_MODE.NOT_VERIFIED);
//						if(linkWithFB && FbToken != null){
//							DataStore.getInstance().connectWithFB(FbToken, null);
//						}
                        showChooseFavCategoriesActivity();
                    }
                } else {
                    // optinonaly we may extract some error message from "data" in some cases
                    Toast.makeText(getApplicationContext(), getString(R.string.err_check_connection), Toast.LENGTH_SHORT).show();
                }

            } catch (Exception c) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_signingin), Toast.LENGTH_SHORT).show();
                attemptingLogin = false;
            }
        }
    };

    @Override
    public void onClick(View arg0) {
        int vId = arg0.getId();

        switch (vId) {
            case R.id.btnFBLogin:
                attempFBtLogin();
                break;
            case R.id.btnEnterPhoneNum:
                attempLogIn();
                break;
            case R.id.btnSignup:
                attempSignUp();
                break;
        }
    }

    public static class FragPhoenNumberForm extends Fragment  implements OnClickListener{

        private TextView tvCountryName, tvCode;
        private EditText etPhoneNum;
        boolean submitting = false;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.frag_enter_num, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            try {
                init((LoginActivity) getActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected void send() {
            try {
                ((LoginActivity) getActivity()).attempLogIn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void init(final LoginActivity activity) {

            etPhoneNum = (EditText) getView().findViewById(R.id.phone);
            tvCode = (TextView) getView().findViewById(R.id.tvCountryCode);
            tvCountryName = (TextView) getView().findViewById(R.id.tvCountryAlphaCode);

            tvCode.setOnClickListener(this);
            tvCountryName.setOnClickListener(this);

            activity.btnEnterPhoneNum = getView().findViewById(R.id.btnEnterPhoneNum);
            View tvEnterNumTitle1 = getView().findViewById(R.id.tvEnterNumTitle1);
            View tvEnterNumTitle2 = getView().findViewById(R.id.tvEnterNumTitle2);

            activity.btnEnterPhoneNum.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    send();
                }
            });
            activity.handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isAdded())
                        activity.hideKeypoard(etPhoneNum);
                }
            }, 1000);

            tvEnterNumTitle1.setAlpha(0);
            tvEnterNumTitle2.setAlpha(0);
            etPhoneNum.setAlpha(0);
            activity.btnEnterPhoneNum.setAlpha(0);

            // Animate in
            ViewAnimator.animate(tvEnterNumTitle1).dp().startDelay(800).alpha(0, 1).translationY(100, 0).duration(400)
                    .interpolator(new OvershootInterpolator())
                    .start();
            ViewAnimator.animate(tvEnterNumTitle2).startDelay(900).alpha(0, 1).dp().translationY(100, 0).duration(400)
                    .interpolator(new OvershootInterpolator())
                    .start();
            ViewAnimator.animate(etPhoneNum).startDelay(1000).alpha(0,1).dp().translationY(100, 0).duration(400)
                    .interpolator(new OvershootInterpolator())
                    .start();
            ViewAnimator.animate(activity.btnEnterPhoneNum).startDelay(1100).alpha(0,1).dp().translationY(100, 0).duration(400)
                    .interpolator(new OvershootInterpolator())
                    .start();
        }

        protected void showCountryPicker() {
            try {
                Intent intent = new Intent(getActivity(), CountryListActivity.class);
                startActivityForResult(intent, LoginActivity.REQ_CODE_COUNTRY_LIST_ACTIVITY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String getPhoneNumber(){
            //String phone = tvCode.getText().toString().substring(1) + etPhoneNum.getText().toString();
            String phone = tvCode.getText().toString() + etPhoneNum.getText().toString();
            return  phone;
        }

        public String getCountryCode (){
            return  tvCountryName.getText().toString();
        }

//        // utils
//        public boolean validatePhoneNum() {
//            String num = etPhoneNum.getText().toString().replaceAll("\\s+", "");
//
//            phoeNumCheckResult result = phoeNumCheckResult.OK;
//
//            if (num == null || "".equals(num.trim()))
//                result = phoeNumCheckResult.EMPTY;
//
//            if (num.length() <= 6) {
//                result = phoeNumCheckResult.SHORT;
//            }
//            if(result != phoeNumCheckResult.OK)
//            {
//                OcirclesApp.showAlert(getActivity(), R.string.err_title_info, R.string.err_incorrect_phone_num, R.string.err_action_ok);
//                return false;
//            }
//            return true;
//        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == Activity.RESULT_OK) {
                if(requestCode == LoginActivity.REQ_CODE_COUNTRY_LIST_ACTIVITY) {
                    tvCountryName.setText(data.getStringExtra("name"));
                    tvCode.setText(data.getStringExtra("code"));
                }else if(requestCode == REQ_CODE_VERIFICATION) {
                    getActivity().finish();
                    DataStore.getInstance().triggerDataUpdate();
                }

            }
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.tvCountryAlphaCode:
                    showCountryPicker();
                    break;
                case R.id.tvCountryCode:
                    showCountryPicker();
                    break;
            }
        }
    }

    public static class FragUserDetailsForm extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.frag_enter_user_details, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            init((LoginActivity) getActivity());
        }

        private void init(final LoginActivity activity) {
            activity.etSignupFName = (EditText) getView().findViewById(R.id.etFirstName);
            activity.etSignupLName = (EditText) getView().findViewById(R.id.etLastName);
            activity.btnFBlogin = getView().findViewById(R.id.btnFBLogin);
            activity.btnSignup = getView().findViewById(R.id.btnSignup);

            activity.btnFBlogin.setOnClickListener(activity);
            activity.btnSignup.setOnClickListener(activity);

//            activity.handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (!isAdded())
//                        return;
//                    activity.hideKeypoard(etPhoneNum);
//                }
//            }, 1000);
        }

    }

    // utils
    public static phoeNumCheckResult validatePhoneNum(String num) {
        if (num == null || "".equals(num.trim()))
            return phoeNumCheckResult.EMPTY;

        phoeNumCheckResult result = phoeNumCheckResult.OK;
        if (num.length() <= 8) {
            result = phoeNumCheckResult.SHORT;
        }

        if (!(num.startsWith("00") || num.startsWith("+")))
            result = phoeNumCheckResult.WRONG;

        return result;
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

}
