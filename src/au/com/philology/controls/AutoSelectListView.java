package au.com.philology.controls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ListView;

@SuppressLint("NewApi")
public class AutoSelectListView extends ListView
{
	public int theSelectedColor = Color.BLUE;

	public AutoSelectListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public AutoSelectListView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public AutoSelectListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public AutoSelectListView(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void autoSelect(int index)
	{
		int count = this.getCount();
		if (count > 0)
		{
			if (index < count && index >= 0)
			{
				this.select(index);
			} else
			{
				index -= 1;
				this.select(index);
			}
		}
	}

	public void select(int index)
	{
		if (index < this.getCount() && index >= 0)
		{
			this.setItemChecked(index, true);
		}
	}
}
