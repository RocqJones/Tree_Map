package tk.paulmburu.treemap.ui.tree

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import tk.paulmburu.treemap.models.Tree
import tk.paulmburu.treemap.repository.FirestoreRepository
import java.io.ByteArrayOutputStream

class TreeViewModel : ViewModel() {
    val TAG = "TREE_VIEW_MODEL"
    val firebaseRepository = FirestoreRepository()

    val storage = Firebase.storage
    // Create a storage reference from our app
    var storageRef = storage.reference


    var savedTrees : MutableLiveData<List<Tree>> = MutableLiveData()
    val _navigateToNiceWorkFragment: MutableLiveData<Boolean> = MutableLiveData()
    val navigaToNiceWorkFragment: LiveData<Boolean>
        get() = _navigateToNiceWorkFragment

    // save address to firebase
    fun saveTreeToFirebase(newTree: Tree, username: String, userEmail: String,treeId: String, treeRegion: String, bitmap: Bitmap){

        firebaseRepository.saveNewArboristTree(newTree,username, userEmail, treeId)
            .addOnFailureListener {
            Log.e(TAG,"Failed to save Arborist's Tree!")
        }
//        firebaseRepository.saveNewArboristTreeRegion(treeRegion,username,userEmail,treeId).addOnFailureListener{
//            Log.e(TAG,"Failed to save Tree Region!")
//        }
        firebaseRepository.addNewArboristTreeToGlobalTrees(newTree).addOnFailureListener {
            Log.e(TAG,"Failed to save Tree to Global Trees!")
        }

        uploadTreeImageToFirebase(bitmap,username,userEmail,treeId)
        _navigateToNiceWorkFragment.value = true
    }

    fun uploadTreeImageToFirebase(bitmap: Bitmap, username: String, userEmail: String, treeId: String){

        val treeRef = storageRef.child("${username}_${userEmail}/trees/${treeId}")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = treeRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Log.d(TAG, "data = ${bitmap} -> Error = : ${it.toString()}")
        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            Log.d(TAG, "bytes transferred = ${it.bytesTransferred}")

        }
    }

    // get realtime updates from firebase regarding saved trees
    fun getSavedAddresses(): LiveData<List<Tree>> {
        firebaseRepository.getSavedAddress().addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                savedTrees.value = null
                return@EventListener
            }

            var savedTreesList : MutableList<Tree> = mutableListOf()
            for (doc in value!!) {
                var tree = doc.toObject(Tree::class.java)
                savedTreesList.add(tree)
            }
            savedTrees.value = savedTreesList
        })

        return savedTrees
    }

    // delete an address from firebase
    fun deleteAddress(tree: Tree){
        firebaseRepository.deleteAddress(tree).addOnFailureListener {
            Log.e(TAG,"Failed to delete Address")
        }
    }

    fun navigateToNiceWorkFragmentComplete(){
        _navigateToNiceWorkFragment.value = false
    }

}