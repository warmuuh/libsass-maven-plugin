package wrm.libsass.api;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface LibSassDll extends Library {

	String returnDLLVersion();

	sass_context sass_new_context();

	sass_file_context sass_new_file_context();

	sass_folder_context sass_new_folder_context();

	void sass_free_context(sass_context ctx);

	void sass_free_file_context(sass_file_context ctx);

	void sass_free_folder_context(sass_folder_context ctx);

	int sass_compile(sass_context ctx);

	int sass_compile_file(sass_file_context ctx);

	int sass_compile_folder(sass_folder_context ctx);

}