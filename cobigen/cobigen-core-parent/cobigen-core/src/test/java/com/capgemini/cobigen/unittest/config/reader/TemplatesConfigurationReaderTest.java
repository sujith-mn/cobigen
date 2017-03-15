package com.capgemini.cobigen.unittest.config.reader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Test;

import com.capgemini.cobigen.api.exception.InvalidConfigurationException;
import com.capgemini.cobigen.api.extension.TriggerInterpreter;
import com.capgemini.cobigen.impl.config.entity.ContainerMatcher;
import com.capgemini.cobigen.impl.config.entity.Increment;
import com.capgemini.cobigen.impl.config.entity.Matcher;
import com.capgemini.cobigen.impl.config.entity.Template;
import com.capgemini.cobigen.impl.config.entity.Trigger;
import com.capgemini.cobigen.impl.config.reader.TemplatesConfigurationReader;

import junit.framework.TestCase;

/**
 * This {@link TestCase} tests the {@link TemplatesConfigurationReader}
 */
public class TemplatesConfigurationReaderTest {

    /**
     * Root path to all resources used in this test case
     */
    private static String testFileRootPath =
        "src/test/resources/testdata/unittest/config/reader/TemplatesConfigurationReaderTest/";

    /**
     * Tests whether all templates of a template package could be retrieved successfully.
     * @throws Exception
     *             test fails
     */
    @Test
    public void testTemplatesOfAPackageRetrieval() throws Exception {

        TemplatesConfigurationReader target =
            new TemplatesConfigurationReader(new File(testFileRootPath + "valid").toPath());

        Trigger trigger = new Trigger("", "asdf", "", Charset.forName("UTF-8"), new LinkedList<Matcher>(),
            new LinkedList<ContainerMatcher>());
        Template templateMock = mock(Template.class);
        HashMap<String, Template> templates = new HashMap<>();
        templates.put("resources_resources_spring_common", templateMock);
        target.loadIncrements(templates, trigger);
    }

    /**
     * Tests that templates will be correctly resolved by the template-scan mechanism.
     * @throws Exception
     *             test fails
     */
    @Test
    public void testTemplateScan() throws Exception {

        // given
        TemplatesConfigurationReader target =
            new TemplatesConfigurationReader(new File(testFileRootPath + "valid").toPath());

        Trigger trigger = new Trigger("", "asdf", "", Charset.forName("UTF-8"), new LinkedList<Matcher>(),
            new LinkedList<ContainerMatcher>());
        TriggerInterpreter triggerInterpreter = null;

        // when
        Map<String, Template> templates = target.loadTemplates(trigger, triggerInterpreter);

        // then
        assertThat(templates).isNotNull().hasSize(6);

        String templateIdFooClass = "prefix_FooClass.java";
        Template templateFooClass = templates.get(templateIdFooClass);
        assertThat(templateFooClass).isNotNull();
        assertThat(templateFooClass.getName()).isEqualTo(templateIdFooClass);
        assertThat(templateFooClass.getRelativeTemplatePath()).isEqualTo("foo/FooClass.java.ftl");
        assertThat(templateFooClass.getUnresolvedTargetPath()).isEqualTo("src/main/java/foo/FooClass.java");
        assertThat(templateFooClass.getMergeStrategy()).isNull();
    }

    /**
     * Tests that the template-scan mechanism does not overwrite an explicit template declaration with the
     * defaults
     * @throws Exception
     *             test fails
     */
    @Test
    public void testTemplateScanDoesNotOverwriteExplicitTemplateDeclarations() throws Exception {
        // given
        TemplatesConfigurationReader target =
            new TemplatesConfigurationReader(new File(testFileRootPath + "valid").toPath());

        Trigger trigger = new Trigger("", "asdf", "", Charset.forName("UTF-8"), new LinkedList<Matcher>(),
            new LinkedList<ContainerMatcher>());
        TriggerInterpreter triggerInterpreter = null;

        // when
        Map<String, Template> templates = target.loadTemplates(trigger, triggerInterpreter);

        // this one is a predefined template and shall not be overwritten by scan...
        String templateIdFoo2Class = "prefix_Foo2Class.java";
        Template templateFoo2Class = templates.get(templateIdFoo2Class);
        assertThat(templateFoo2Class).isNotNull();
        assertThat(templateFoo2Class.getName()).isEqualTo(templateIdFoo2Class);
        assertThat(templateFoo2Class.getRelativeTemplatePath()).isEqualTo("foo/Foo2Class.java.ftl");
        assertThat(templateFoo2Class.getUnresolvedTargetPath())
            .isEqualTo("src/main/java/foo/Foo2Class${variable}.java");
        assertThat(templateFoo2Class.getMergeStrategy()).isEqualTo("javamerge");

        String templateIdBarClass = "prefix_BarClass.java";
        Template templateBarClass = templates.get(templateIdBarClass);
        assertThat(templateBarClass).isNotNull();
        assertThat(templateBarClass.getName()).isEqualTo(templateIdBarClass);
        assertThat(templateBarClass.getRelativeTemplatePath()).isEqualTo("foo/bar/BarClass.java.ftl");
        assertThat(templateBarClass.getUnresolvedTargetPath()).isEqualTo("src/main/java/foo/bar/BarClass.java");
        assertThat(templateBarClass.getMergeStrategy()).isNull();
    }

    /**
     * Tests the overriding of all possible attributes by templateExtensions
     * @throws Exception
     *             test fails
     */
    @Test
    public void testTemplateExtensionDeclarationOverridesTemplateScanDefaults() throws Exception {
        // given
        TemplatesConfigurationReader target =
            new TemplatesConfigurationReader(new File(testFileRootPath + "valid").toPath());

        Trigger trigger = new Trigger("", "asdf", "", Charset.forName("UTF-8"), new LinkedList<Matcher>(),
            new LinkedList<ContainerMatcher>());
        TriggerInterpreter triggerInterpreter = null;

        // when
        Map<String, Template> templates = target.loadTemplates(trigger, triggerInterpreter);

        // validation

        // check scan default as precondition for this test. If they change, this test might be worth to be
        // adapted
        String templateIdBarClass = "prefix2_BarClass.java";
        Template templateBarClass = templates.get(templateIdBarClass);
        assertThat(templateBarClass).isNotNull();
        // template-scan defaults
        assertThat(templateBarClass.getName()).isEqualTo(templateIdBarClass);
        assertThat(templateBarClass.getRelativeTemplatePath()).isEqualTo("bar/BarClass.java.ftl");
        assertThat(templateBarClass.getUnresolvedTargetPath()).isEqualTo("src/main/java/bar/BarClass.java");
        assertThat(templateBarClass.getMergeStrategy()).isNull();
        assertThat(templateBarClass.getTargetCharset()).isEqualTo("UTF-8");

        // check defaults overwriting by templateExtensions
        String templateIdFooClass = "prefix2_FooClass.java";
        Template templateFooClass = templates.get(templateIdFooClass);
        assertThat(templateFooClass).isNotNull();
        // template-scan defaults
        assertThat(templateFooClass.getName()).isEqualTo(templateIdFooClass);
        assertThat(templateFooClass.getRelativeTemplatePath()).isEqualTo("bar/FooClass.java.ftl");
        // overwritten by templateExtension
        assertThat(templateFooClass.getUnresolvedTargetPath()).isEqualTo("adapted/path/FooClass.java");
        assertThat(templateFooClass.getMergeStrategy()).isEqualTo("javamerge");
        assertThat(templateFooClass.getTargetCharset()).isEqualTo("ISO-8859-1");
    }

    /**
     * Tests an empty templateExtensions does not override any defaults
     * @throws Exception
     *             test fails
     */
    @Test
    public void testEmptyTemplateExtensionDeclarationDoesNotOverrideAnyDefaults() throws Exception {
        // given
        TemplatesConfigurationReader target =
            new TemplatesConfigurationReader(new File(testFileRootPath + "valid").toPath());

        Trigger trigger = new Trigger("", "asdf", "", Charset.forName("UTF-8"), new LinkedList<Matcher>(),
            new LinkedList<ContainerMatcher>());
        TriggerInterpreter triggerInterpreter = null;

        // when
        Map<String, Template> templates = target.loadTemplates(trigger, triggerInterpreter);

        // validation
        String templateIdFooClass = "prefix2_Foo2Class.java";
        Template templateFooClass = templates.get(templateIdFooClass);
        assertThat(templateFooClass).isNotNull();
        // template-scan defaults
        assertThat(templateFooClass.getName()).isEqualTo(templateIdFooClass);
        assertThat(templateFooClass.getRelativeTemplatePath()).isEqualTo("bar/Foo2Class.java.ftl");
        assertThat(templateFooClass.getUnresolvedTargetPath()).isEqualTo("src/main/java/bar/Foo2Class.java");
        assertThat(templateFooClass.getMergeStrategy()).isNull();
        assertThat(templateFooClass.getTargetCharset()).isEqualTo("UTF-8");
    }

    /**
     * Tests whether an invalid configuration results in an {@link InvalidConfigurationException}
     * @throws InvalidConfigurationException
     *             expected
     */
    @Test(expected = InvalidConfigurationException.class)
    public void testErrorOnInvalidConfiguration() throws InvalidConfigurationException {

        new TemplatesConfigurationReader(new File(testFileRootPath + "faulty").toPath());
    }

    /**
     * Tests whether a duplicate template extension declaration will result in an
     * {@link InvalidConfigurationException}
     * @throws InvalidConfigurationException
     *             expected
     */
    @Test(expected = InvalidConfigurationException.class)
    public void testErrorOnDuplicateTemplateExtensionDeclaration() throws InvalidConfigurationException {

        TemplatesConfigurationReader reader = new TemplatesConfigurationReader(
            new File(testFileRootPath + "faulty_duplicate_template_extension").toPath());
        reader.loadTemplates(null, null);
    }

    /**
     * Tests whether a template extension with an id-reference, which does not point on any template, will
     * cause an {@link InvalidConfigurationException}
     * @throws InvalidConfigurationException
     *             expected
     */
    @Test(expected = InvalidConfigurationException.class)
    public void testErrorOnUnhookedTemplateExtensionDeclaration() throws InvalidConfigurationException {

        TemplatesConfigurationReader reader = new TemplatesConfigurationReader(
            new File(testFileRootPath + "faulty_unhooked_template_extension").toPath());
        reader.loadTemplates(null, null);
    }

    /**
     * Tests whether a two equally named files will result in an {@link InvalidConfigurationException} if they
     * are scanned with the same prefix
     * @throws InvalidConfigurationException
     *             expected
     */
    @Test(expected = InvalidConfigurationException.class)
    public void testErrorOnDuplicateScannedIds() throws InvalidConfigurationException {

        TemplatesConfigurationReader reader =
            new TemplatesConfigurationReader(new File(testFileRootPath + "faulty_duplicate_scanned_id").toPath());
        reader.loadTemplates(null, null);
    }

    /**
     * Tests the correct resolution of template scan references in increments.
     * @throws InvalidConfigurationException
     *             test fails
     */
    @Test
    public void testCorrectResolutionOfTemplateScanReferences() throws InvalidConfigurationException {

        // given
        TemplatesConfigurationReader reader =
            new TemplatesConfigurationReader(new File(testFileRootPath + "valid_template_scan_references").toPath());

        Trigger trigger = new Trigger("", "asdf", "", Charset.forName("UTF-8"), new LinkedList<Matcher>(),
            new LinkedList<ContainerMatcher>());
        TriggerInterpreter triggerInterpreter = null;

        // when
        Map<String, Template> templates = reader.loadTemplates(trigger, triggerInterpreter);
        Map<String, Increment> increments = reader.loadIncrements(templates, trigger);

        // validation
        assertThat(templates).containsOnlyKeys("prefix_foo_BarClass.java", "prefix_bar_Foo2Class.java",
            "prefix_foo_FooClass.java");
        assertThat(increments).containsOnlyKeys("test");
        assertThat(increments.get("test").getTemplates()).extracting("name").containsOnly("prefix_foo_BarClass.java",
            "prefix_foo_FooClass.java");
    }

    /**
     * Tests the correct detection of duplicate template scan names.
     * @throws InvalidConfigurationException
     *             expected
     */
    @Test(expected = InvalidConfigurationException.class)
    public void testErrorOnDuplicateTemplateScanNames() throws InvalidConfigurationException {

        TemplatesConfigurationReader reader = new TemplatesConfigurationReader(
            new File(testFileRootPath + "faulty_duplicate_template_scan_name").toPath());
        reader.loadTemplates(null, null);
    }

    /**
     * Tests the correct detection of invalid template scan references.
     * @throws InvalidConfigurationException
     *             expected
     */
    @Test(expected = InvalidConfigurationException.class)
    public void testErrorOnInvalidTemplateScanReference() throws InvalidConfigurationException {

        TemplatesConfigurationReader reader =
            new TemplatesConfigurationReader(new File(testFileRootPath + "faulty_invalid_template_scan_ref").toPath());
        reader.loadTemplates(null, null);
    }

    /**
     * Tests the correct resolution of references of templates / templateScans / increments.
     */
    @Test
    public void testIncrementComposition_combiningAllPossibleReferences() {

        // given
        TemplatesConfigurationReader target =
            new TemplatesConfigurationReader(new File(testFileRootPath + "valid_increment_composition").toPath());

        Trigger trigger = new Trigger("", "asdf", "", Charset.forName("UTF-8"), new LinkedList<Matcher>(),
            new LinkedList<ContainerMatcher>());
        TriggerInterpreter triggerInterpreter = null;

        // when
        Map<String, Template> templates = target.loadTemplates(trigger, triggerInterpreter);
        Map<String, Increment> increments = target.loadIncrements(templates, trigger);

        // validation

        assertThat(templates).containsOnlyKeys("templateDecl", "prefix_scanned", "scanned", "prefix_scanned2",
            "scanned2");
        assertThat(increments).containsOnlyKeys("0", "1", "2");
        assertThat(increments.values()).hasSize(3);
        assertThat(increments.get("0").getTemplates()).extracting("name").containsOnly("templateDecl");
        assertThat(increments.get("1").getTemplates()).extracting("name").containsOnly("templateDecl", "prefix_scanned",
            "scanned", "scanned2");
        assertThat(increments.get("2").getTemplates()).extracting("name").containsOnly("templateDecl", "prefix_scanned",
            "scanned", "prefix_scanned2");

    }

    /**
     * Test for <a href="https://github.com/devonfw/tools-cobigen/issues/167">Issue 167</a>. Tests if the
     * exception message from {@link #testErrorOnDuplicateScannedIds()} contains the name of the file causing
     * the exception
     */
    @Test
    public void testExceptionMessageForDuplicateTemplateNames() {
        String message = "";
        try {
            testErrorOnDuplicateScannedIds();
            fail("An Exception should have been thrown");
        } catch (Exception e) {
            message = e.getMessage();
        }
        assertFalse(message.indexOf("Bar") == -1);
    }

    /**
     * Test of <a href="https://github.com/devonfw/tools-cobigen/issues/157">issue 157</a> for relocation of
     * templates to support multi-module generation.
     */
    @Test
    public void testRelocate() {

        // given
        String noRelocation = "";
        String relocation = "../api/";
        String pathname = testFileRootPath + "valid_relocate/";
        TemplatesConfigurationReader target = new TemplatesConfigurationReader(new File(pathname).toPath());

        Trigger trigger = new Trigger("id", "type", "valid_relocate", Charset.forName("UTF-8"),
            new LinkedList<Matcher>(), new LinkedList<ContainerMatcher>());
        TriggerInterpreter triggerInterpreter = null;

        // when
        Map<String, Template> templates = target.loadTemplates(trigger, triggerInterpreter);
        Map<String, Increment> increments = target.loadIncrements(templates, trigger);

        // validation
        int templateCount = 0;
        templateCount++;
        Template entityName = verifyTemplate(templates, "__EntityName__.java",
            "java/__rootpackage__/__component__/common/api/", pathname, relocation);
        assertThat(entityName.getVariables()).hasSize(2).containsEntry("foo", "common.api").containsEntry("relocate",
            "../api/src/main/${cwd}");

        templateCount++;
        Template entityNameEntity = verifyTemplate(templates, "__EntityName__Entity.java",
            "java/__rootpackage__/__component__/dataaccess/api/", pathname, noRelocation);
        assertThat(entityNameEntity.getVariables()).hasSize(1).containsEntry("foo", "root");

        templateCount++;
        Template entityNameEto = verifyTemplate(templates, "__EntityName__Eto.java",
            "java/__rootpackage__/__component__/logic/api/to/", pathname, relocation);
        assertThat(entityNameEto.getVariables()).hasSize(2).containsEntry("foo", "logic.api.to")
            .containsEntry("relocate", "../api/src/main/${cwd}");

        templateCount++;
        Template component = verifyTemplate(templates, "__Component__.java",
            "java/__rootpackage__/__component__/logic/api/", pathname, noRelocation);
        assertThat(component.getVariables()).hasSize(1).containsEntry("foo", "root");

        templateCount++;
        Template componentImpl = verifyTemplate(templates, "__Component__Impl.java",
            "java/__rootpackage__/__component__/logic/impl/", pathname, noRelocation);
        assertThat(componentImpl.getVariables()).hasSize(1).containsEntry("foo", "root");

        assertThat(templates).hasSize(templateCount);

        Increment increment = increments.get("test");
        assertThat(increment).isNotNull();
        assertThat(increment.getName()).isEqualTo("test");
        assertThat(increment.getDescription()).isEqualTo("TEST");
        assertThat(increment.getTemplates()).hasSize(templates.size()).containsAll(templates.values());
    }

    private Template verifyTemplate(Map<String, Template> templates, String name, String path, String rootPath,
        String relocation) {

        Template template = templates.get(name);
        assertThat(template).isNotNull();
        String pathWithName = path + name;
        assertThat(template.getRelativeTemplatePath()).isEqualTo(pathWithName);
        assertThat(template.getAbsoluteTemplatePath().toString().replace('\\', '/')).isEqualTo(rootPath + pathWithName);
        assertThat(template.getUnresolvedTemplatePath()).isEqualTo("src/main/" + pathWithName);
        assertThat(template.getUnresolvedTargetPath()).isEqualTo(relocation + "src/main/" + pathWithName);
        return template;
    }
}
