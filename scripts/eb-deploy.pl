#!/usr/bin/perl

# We are going to alter project.clj and resounse/public/index.html and then checkout them out again.
# So make sure it is not currently modified.
@diffs = qx(git diff --name-only);
print "Diffs: @diffs\n";
foreach $file (@diffs) {
	chomp($file);
	if ($file =~ /^project.clj$/ or $file =~ /^resources\/public\/index.html$/) {
		print STDERR "Changes to $file should be commited before deployment.\n";
		exit(1);
	}
}
