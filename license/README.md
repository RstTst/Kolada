# License
The MIT license (given in full in [LICENSE.txt](LICENSE.txt)) applies to all code in this repository
which is copyright by RstTst.


## Fat-Jars
Some builds of Kolada may be distributed as 'Fat-Jars', that include byte code of third-party libraries. The ['fat-jar' directory](fat-jar) contains
licenses and licensing related files that have to be included in the source of Kolada as a consequence of this.
These do not directly apply to Kolada:
* **Kotlin Standard Library + Kotlin Reflection:**
    + Copyright 2010-2018 JetBrains s.r.o.*
    + License: [Apache License Version 2.0*](fat-jar/Apache-2.0-LICENSE.txt)
    + Notice as required by the Apache License Version 2.0, section 4(d): [Kotlin_Compiler-NOTICE](fat-jar/Kotlin_Compiler-NOTICE.txt)
    + Source: [GitHub Repository](https://github.com/JetBrains/kotlin).
    
    *Some parts of Kotlin contain third-party code to which different licenses may apply (details on [GitHub](https://github.com/JetBrains/kotlin/tree/b30537de0e736b28b1ae2cbf0d34d42b6733f795/license)).
    *Only code that requires the redistribution of its license with its compiled form is listed here. Paths are relative to the [GitHub repository](https://github.com/JetBrains/kotlin) of Kotlin.*
    + Path: core/reflection.jvm/src/kotlin.reflect/jvm/internal/pcollections
        + License: [MIT License](fat-jar/PCollections-LICENSE.txt)
        + Origin: Derived from PCollections, A Persistent Java Collections Library (https://pcollections.org/)
    + Path: libraries/stdlib/src/kotlin/collections
        + License: [Apache License Version 2.0](fat-jar/Apache-2.0-LICENSE.txt)
        + Origin: Derived from GWT, (C) 2007-08 Google Inc.
    + Path: libraries/stdlib/unsigned/src/kotlin/UnsignedUtils.kt
        + License: [Apache License Version 2.0](fat-jar/Apache-2.0-LICENSE.txt)
        + Origin: Derived from Guava's UnsignedLongs, (C) 2011 The Guava Authors
* **JetBrains Annotation Library**
    + Copyright 2000-2016 JetBrains s.r.o.
    + License: [Apache License Version 2.0](fat-jar/Apache-2.0-LICENSE.txt)
    + Source: [GitHub repository](https://github.com/JetBrains/java-annotations)
* **Kotlinx Coroutines Core:**  
    + Copyright 2016-2019 JetBrains s.r.o.
    + License: [Apache License Version 2.0](fat-jar/Apache-2.0-LICENSE.txt)
    + Notice as required by the Apache License Version 2.0, section 4(d): [kotlinx.coroutines-NOTICE](fat-jar/kotlinx.coroutines-NOTICE.txt)
    + Source: [GitHub Repository](https://github.com/JetBrains/kotlin).
* **Kotlinx Serialization Runtime:**
    + Copyright 2017-2019 JetBrains s.r.o 
    + License: [Apache License Version 2.0](fat-jar/Apache-2.0-LICENSE.txt)
    + Notice as required by the Apache License Version 2.0, section 4(d): [kotlinx.serialization-NOTICE](fat-jar/kotlinx.serialization-NOTICE.txt)
    + Source: [GitHub Repository](https://github.com/Kotlin/kotlinx.serialization)










        