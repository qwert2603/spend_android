package android.support.design.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class TextInputAutoCompleteTextView extends AppCompatAutoCompleteTextView {

    public TextInputAutoCompleteTextView(Context context) {
        super(context);
    }

    public TextInputAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextInputAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public CharSequence getHint() {
        TextInputLayout layout = this.getTextInputLayout();
        return layout != null && layout.isProvidingHint() ? layout.getHint() : super.getHint();
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection ic = super.onCreateInputConnection(outAttrs);
        if (ic != null && outAttrs.hintText == null) {
            outAttrs.hintText = this.getHintFromLayout();
        }

        return ic;
    }

    @Nullable
    private TextInputLayout getTextInputLayout() {
        for(ViewParent parent = this.getParent(); parent instanceof View; parent = parent.getParent()) {
            if (parent instanceof TextInputLayout) {
                return (TextInputLayout)parent;
            }
        }

        return null;
    }

    @Nullable
    private CharSequence getHintFromLayout() {
        TextInputLayout layout = this.getTextInputLayout();
        return layout != null ? layout.getHint() : null;
    }
}
