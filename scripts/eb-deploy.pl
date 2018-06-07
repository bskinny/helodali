#!/usr/bin/perl

# We are going to alter project.clj and resounse/public/index.html and then checkout them out again.
# So make sure it is not currently modified.
use strict;
use warnings FATAL => 'all';

my @diffs = qx(git diff --name-only);
foreach my $file (@diffs) {
	chomp($file);
	if ($file =~ /^project.clj$/ or $file =~ /^resources\/public\/index.html$/) {
		print STDERR "Changes to $file should be commited before deployment.\n";
		exit(1);
	}
}

my $ct = time(); # Seconds since epoch
qx(perl -p -i -e "s/app.js/app-$ct.js/g" project.clj);
qx(perl -p -i -e "s/app.js/app-$ct.js/g" resources/public/index.html);

# Build the war file and rename it
qx(lein clean);
qx(lein with-profile webapp ring uberwar);
my $war = "helodali-$ct.war";
qx(mv target/helodali.war "target/$war");

# Deploy the war
print qx(aws s3 cp "target/$war" s3://elasticbeanstalk-us-east-1-128225160927/);
print qx(aws elasticbeanstalk create-application-version --application-name helodali --version-label "v$ct" --source-bundle S3Bucket="elasticbeanstalk-us-east-1-128225160927",S3Key="$war");
print qx(aws elasticbeanstalk update-environment --application-name helodali --environment-name Helodali-env --version-label "v$ct");

qx(git checkout project.clj resources/public/index.html);

