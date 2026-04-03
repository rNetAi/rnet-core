package io.github.rNetAi.rnetCore.scanner.processor;

import com.google.auto.service.AutoService;
import io.github.rNetAi.rnetCore.scanner.annotations.RNetEndpoint;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("io.github.rNetAi.rnetCore.scanner.annotations.RNetEndpoint")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class RNetEndpointProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element element : roundEnv.getElementsAnnotatedWith(RNetEndpoint.class)) {

            if (element.getKind() != ElementKind.METHOD) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "@RNetEndpoint can only be applied to methods",
                        element
                );
                continue;
            }

            ExecutableElement method = (ExecutableElement) element;

            for (TypeMirror thrownType : method.getThrownTypes()) {
                String thrownTypeName = thrownType.toString();

                if (!thrownTypeName.equals("io.github.rNetAi.rnetCore.rNetProtocol.exception.RNetException")) {
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            "@RNetEndpoint method '" + method.getSimpleName() +
                                    "' may only declare 'throws RNetException', found: " + thrownTypeName,
                            element
                    );
                }
            }
        }

        return true;
    }
}