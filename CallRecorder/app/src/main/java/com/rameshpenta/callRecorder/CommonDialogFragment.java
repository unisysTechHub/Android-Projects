package com.rameshpenta.callRecorder;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.net.PasswordAuthentication;
import java.security.KeyStore;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CommonDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class CommonDialogFragment extends DialogFragment {

    private OnFragmentInteractionListener mListener;
    private  DeleteOnClickListener deleteOnClickListener;


    public  interface DeleteOnClickListener
    {
               public void onConfirmDelete();
               public  void onConfirmEmptyInbox();
               public  void onConfirmTrashDelete();
               public  void onConfirmTrashEmpty();
               public  void checkPassword(String password);
               public void setPassword(String setPassword);
                public  void setEmail(String Email);
                public void   getEmail();
                public void setDate(long date);

                public void sendEmail(String EmailId);



    }
    public CommonDialogFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogFragment = null;
        System.out.println("Ramesh CommonDialogFragment onCreateDialog ");
        Bundle bundle = this.getArguments();
        boolean deleteInbox = bundle.getBoolean("DeleteInbox", false);
        boolean emptyInbox = bundle.getBoolean("EmptyInbox", false);
        boolean deleteTrash = bundle.getBoolean("DeleteTrash", false);
        boolean emptyTrash = bundle.getBoolean("EmptyTrash", false);
        boolean passwordCheck = bundle.getBoolean("Password",false);
        boolean setPassword = bundle.getBoolean("setPassword",false);
        boolean setEmail    = bundle.getBoolean("setEmail",false);
        boolean displayMessage = bundle.getBoolean("displayMessage",false);
        boolean setDate = bundle.getBoolean("DatePicker",false);
        boolean displayDateErrorMsg= bundle.getBoolean("DisplayDateMessage",false);

        if(displayDateErrorMsg)
        {

            dialogFragment = new AlertDialog.Builder(getActivity());
            dialogFragment.setTitle("Set Date should be Future Date");
            dialogFragment.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }
        if(setDate)
        {   dialogFragment = new AlertDialog.Builder(getActivity());
            dialogFragment.setTitle("Set Date ");
            ;
           final  DatePicker datePicker = new DatePicker(getActivity());
            dialogFragment.setView(datePicker);
            dialogFragment.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {


                    //date=datePicker.getCalendarView().getDate();


                    String dateText = Integer.valueOf(datePicker.getYear()).toString();

                    if (datePicker.getMonth() + 1 > 9)
                        dateText = dateText + Integer.valueOf(datePicker.getMonth() + 1).toString();
                    else
                        dateText = dateText + "0" + Integer.valueOf(datePicker.getMonth() + 1).toString();

                    if(datePicker.getDayOfMonth() > 9
                            )
                    dateText = dateText + Integer.valueOf(datePicker.getDayOfMonth()).toString();
                    else
                        dateText = dateText + "0"+Integer.valueOf(datePicker.getDayOfMonth()).toString();

                    deleteOnClickListener.setDate(Long.parseLong(dateText));
                }
            });
            dialogFragment.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            dialogFragment.setNeutralButton("None", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteOnClickListener.setDate(0);
                }
            });
        }

        if(displayMessage)
        {
            dialogFragment = new AlertDialog.Builder(getActivity());
            final String emailId=bundle.getString("EmailId",null);
            dialogFragment.setTitle("Password will be sent to " + emailId);

            dialogFragment.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {


                    deleteOnClickListener.sendEmail(emailId);
                }

            });

            dialogFragment.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });



        }
        if(setEmail)
        {
            dialogFragment = new AlertDialog.Builder(getActivity());
            dialogFragment.setTitle("Enter Email Id ");

            final EditText Email = new EditText(getActivity());

            dialogFragment.setView(Email);

            dialogFragment.setPositiveButton("Set Email", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {


                    deleteOnClickListener.setEmail(Email.getText().toString().trim());
                }

            });



        }



        if(setPassword)
        {
            dialogFragment = new AlertDialog.Builder(getActivity());
            dialogFragment.setTitle("Enter App Password ");

            final EditText password = new EditText(getActivity());
            password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            dialogFragment.setView(password);

            dialogFragment.setPositiveButton("Set Password", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {


                    deleteOnClickListener.setPassword(password.getText().toString().trim());
                }

            });



        }


        if(passwordCheck)
        {

            dialogFragment = new AlertDialog.Builder(getActivity());
            dialogFragment.setTitle("Enter App Password");

            final EditText password = new EditText(getActivity());
            password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
           password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            dialogFragment.setView(password);
            dialogFragment.setNegativeButton("ForgotPassword", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   deleteOnClickListener.getEmail();


                }
            });
            dialogFragment.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {


                    deleteOnClickListener.checkPassword(password.getText().toString().trim());
                }
            });


        }

        if (deleteInbox) {
            dialogFragment = new AlertDialog.Builder(getActivity());

            dialogFragment.setTitle("Confirm to Delete Selected CallRecords");

            dialogFragment.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteOnClickListener.onConfirmDelete();
                }
            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });



        } else if (emptyInbox) {

            dialogFragment = new AlertDialog.Builder(getActivity());
            dialogFragment.setTitle("Inbox Empty Confirmation");
            dialogFragment.setTitle("Confirm to Empty Inbox");
            dialogFragment.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteOnClickListener.onConfirmEmptyInbox();
                }
            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });


        }

        if (deleteTrash)
        {
            dialogFragment = new AlertDialog.Builder(getActivity());
            dialogFragment.setTitle("Confirm to Delete Selected Records");
            dialogFragment.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteOnClickListener.onConfirmTrashDelete();

                }
            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

        }

        if (emptyTrash)
        {
            dialogFragment = new AlertDialog.Builder(getActivity());
            dialogFragment.setTitle("Confirm to Empty Trash");
            dialogFragment.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteOnClickListener.onConfirmTrashEmpty();

                }
            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

        }





        return dialogFragment.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        System.out.println("Ramesh CommonDialogFragment onAttach");
        try {
            mListener = (OnFragmentInteractionListener) activity;
            deleteOnClickListener= (DeleteOnClickListener) activity;


        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        System.out.println("Ramesh CommonDialogFragment onDetach");
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
