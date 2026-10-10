/*
 * Annotace
 * Copyright (C) 2026 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.annotace.lemmatizer;

import cz.cvut.kbss.annotace.configuration.MorphoditaConf;
import cz.cvut.kbss.annotace.configuration.SparkConf;
import cz.cvut.kbss.textanalysis.lemmatizer.LemmatizerApi;
import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

// To enable, specify java.library.path containing libmorphodita_java.so (in build.gradle) and set
// absolute paths to taggers in src/test/resources/application.yml
//@Disabled
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class,
    classes = {MorphoditaConf.class, SparkConf.class, MorphoDitaServiceJNI.class, SparkLemmatizer.class})
public class LemmatizationTests {

    @Autowired
    private SparkLemmatizer sut1;

    @Autowired
    private MorphoDitaServiceJNI sut2;

    private LemmatizerApi[] lemmatizers;

    @BeforeEach
    public void init() {
        this.lemmatizers = new LemmatizerApi[] {sut1, sut2};
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Budovou se rozumí nadzemní stavba. Bez střechy se nejedná o budovu.",
        "Kromě toho, že je králem severu, je John Snow anglickým lékařem a lídrem ve vývoji "
            + "anestezie a lékařské hygieny.",
        "Květinou ženu neuhodíš."
    })
    void check(final String val) {
        final List<LemmatizerResult> results = new ArrayList<>();
        Arrays.stream(lemmatizers).forEach(l -> results.add(l.process(val, "cs")));
        results.forEach(r -> {
            System.out.println(r.getLemmatizer());
            r.getResult().forEach(l -> {
                l.forEach(x -> System.out.printf("%1$" + 30 + "s", x.getLemma()));
                System.out.println();
            });
        });
    }
}