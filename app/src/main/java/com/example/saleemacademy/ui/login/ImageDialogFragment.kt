import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.example.saleemacademy.R

class ImageDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_IMAGE_URL = "gs://saleem-academy-login.appspot.com/memos"
        private const val TAG = "ImageDialogFragment"

        fun newInstance(imageUrl: String): ImageDialogFragment {
            val fragment = ImageDialogFragment()
            val args = Bundle()
            args.putString(ARG_IMAGE_URL, imageUrl)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_dialog2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUrl = arguments?.getString(ARG_IMAGE_URL)

        // Load image from Firebase Storage into ImageView using Picasso
        val imageView = view.findViewById<ImageView>(R.id.dialogImageView)
        imageUrl?.let {
            Picasso.get().load(it).into(imageView, object : Callback {
                override fun onSuccess() {
                    // Image loaded successfully
                    Log.d(TAG, "Image loaded successfully")
                }

                override fun onError(e: Exception?) {
                    // Failed to load image
                    Log.e(TAG, "Error loading image", e)
                }
            })
        }
    }
}
