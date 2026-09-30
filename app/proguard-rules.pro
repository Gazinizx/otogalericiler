# Keep Firestore data models
-keep class com.example.anadolugalericilersit.data.model.** { *; }
-keepclassmembers class com.example.anadolugalericilersit.data.model.** { *; }

# Keep LocalStore models
-keep class com.example.anadolugalericilersit.data.local.** { *; }

# Keep Firebase Firestore & Storage serializers
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
