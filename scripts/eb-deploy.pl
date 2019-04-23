#!/usr/bin/perl

# Usage:
# eb-deploy.pl [--ignore-changes] [environment name]
# e.g. eb-deploy.pl uatest

# This script is intended for use in deploying the helodali app to AWS Elastic
# Beanstalk. The eb client should be installed and initialized with the application
# target (e.g. helodali-prod, helodali-test). Specifically, `eb init` should have
# been executed prior to this. Make sure all changes are committed as `eb deploy`
# will use git archive.
#
# If an environment name is not provided to this script, then the default
# environment is targeted.

use strict;
use Term::ANSIColor;
use warnings FATAL => 'all';


# Make sure the AWS eb cli is installed
qx(eb --version) or die "Must install aws eb cli, see README.MD";

$ENV{AWS_PROFILE} = "default";

# Sanity check of uncommitted changes
if (@ARGV and $ARGV[0] =~ /--ignore-changes/i) {
    # Remove the arg from ARGV
    shift(@ARGV);

} else {
    # Look for any uncommitted changes and exit if found.
    my @diffs = qx(git diff HEAD --name-only);
    if (@diffs) {
        printError("Changes to the following files should be committed before deployment.\n");
        foreach my $file (@diffs) {
            print $file;
        }
        printWarning("Override this check with --ignore-changes\n");
        exit 1;
    }
}

# Perform an eb status
print qx(eb status);
if ($? != 0) {
    printError("Unable to execute eb status: $!\n");
    exit 1;
};

my $env = "";
(@ARGV == 1) and $env = $ARGV[0];

print "Continue with deploy (y|n)? [y] ";
my $response = <STDIN>;
chomp($response);
if ($response !~ /^y?$/i) {
    printWarning("Exiting without deployment\n");
    exit 0;
}

# Clean out the target of any previous build as we do not want to rebuild in the container
qx(lein clean);
if ($? != 0) {
    printError("Unable to lein clean: $!\n");
    exit 1;
};

# Deploy the project and let EB build + run the docker container
print qx(eb deploy $env);
if ($? != 0) {
    printError("Unable to deploy: $!\n");
    exit 1;
}


sub printWarning {
    my $msg = shift;
    print color('bold yellow'), $msg, color('reset');
}

sub printError {
    my $msg = shift;
    print color('bold red'), $msg, color('reset');
}