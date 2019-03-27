package com.bmuschko.gradle.docker.utils

import com.bmuschko.gradle.docker.utils.fixtures.AnnotatedClassWithMainMethod
import com.bmuschko.gradle.docker.utils.fixtures.ClassWithMainMethod
import com.bmuschko.gradle.docker.utils.fixtures.ClassWithoutMainMethod
import com.bmuschko.gradle.docker.utils.fixtures.TestJarFile
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class MainClassFinderTest extends Specification {
    private static final String ANNOTATION_CLASS_NAME = "com.bmuschko.gradle.docker.utils.fixtures.SomeApplication"

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    TestJarFile testJarFile

    void setup() throws IOException {
        testJarFile = new TestJarFile(temporaryFolder)
    }

    def "can find annotated main class"() {
        given:
        testJarFile.addClass('a/B.class', ClassWithMainMethod)
        testJarFile.addClass('a/b/c/E.class', AnnotatedClassWithMainMethod)

        when:
        String mainClass = MainClassFinder.findSingleMainClass(testJarFile.getJarSource(), ANNOTATION_CLASS_NAME)

        then:
        mainClass == 'a.b.c.E'
    }

    def "throws exception if annotated main class cannot be found"() {
        given:
        testJarFile.addClass('a/B.class', ClassWithMainMethod)
        testJarFile.addClass('a/b/c/E.class', ClassWithMainMethod)

        when:
        MainClassFinder.findSingleMainClass(testJarFile.getJarSource(), ANNOTATION_CLASS_NAME)

        then:
        def e = thrown(IllegalStateException)
        e.message == 'Unable to find a single main class from the following candidates [a.B, a.b.c.E]'
    }

    def "returns null if no main class can be found"() {
        given:
        testJarFile.addClass('a/B.class', ClassWithoutMainMethod)
        testJarFile.addClass('a/b/c/E.class', ClassWithoutMainMethod)

        when:
        String mainClass = MainClassFinder.findSingleMainClass(testJarFile.getJarSource(), ANNOTATION_CLASS_NAME)

        then:
        !mainClass
    }

    def "only consider .class files"() {
        given:
        testJarFile.addClass('a/b/c/E.class', AnnotatedClassWithMainMethod)
        new File(testJarFile.jarSource, 'noClass.txt').createNewFile()

        when:
        MainClassFinder.findSingleMainClass(testJarFile.getJarSource(), ANNOTATION_CLASS_NAME)

        then:
        noExceptionThrown()
    }
}
