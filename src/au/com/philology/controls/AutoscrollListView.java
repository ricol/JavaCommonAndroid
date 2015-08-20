package au.com.philology.controls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

@SuppressLint("NewApi")
public class AutoscrollListView extends ListView
{

	public AutoscrollListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public AutoscrollListView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public AutoscrollListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public AutoscrollListView(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void scrollToBottom()
	{
		this.post(new Runnable()
		{
			@Override
			public void run()
			{
				// Select the last row so it will scroll into view...
				setSelection(getAdapter().getCount() - 1);
			}
		});
	}
}
