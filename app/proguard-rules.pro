# Application & Activity Entry Points
-keep public class com.example.anadolugalericilersit.AnadoluApp { *; }
-keep public class com.example.anadolugalericilersit.MainActivity { *; }
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity

# Keep Compose Multiplatform generated resources
-keep class com.example.anadolugalericilersit.resources.** { *; }
-keep class org.jetbrains.compose.resources.** { *; }

# Keep Firestore & App Data Models
-keep class com.example.anadolugalericilersit.data.** { *; }
-keepclassmembers class com.example.anadolugalericilersit.data.** { *; }

# Keep ViewModels & Navigation
-keep class com.example.anadolugalericilersit.ui.viewmodel.** { *; }
-keep class com.example.anadolugalericilersit.navigation.** { *; }
-keep class com.example.anadolugalericilersit.utils.** { *; }

# Keep Enums (Firestore deserialization & String value conversion)
-keepclassmembers enum com.example.anadolugalericilersit.** { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Firebase Firestore, Auth, Storage, Messaging
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep Coroutines & Tasks
-keep class kotlinx.coroutines.** { *; }

# Keep Coil & Image Loading
-keep class coil.** { *; }

# Keep ZXing & CameraX
-keep class com.google.zxing.** { *; }
-keep class androidx.camera.** { *; }

# Keep General Serializers & Reflection Attributes
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

