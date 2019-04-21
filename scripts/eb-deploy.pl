#!/usr/bin/perl

# Usage:
# eb-deploy.pl [environment name]
# e.g. eb-deploy.pl uatest

# NOTES:
# `eb init` should have been executed prior to this.
# Make sure all changes are committed as `eb deploy` will use git archive.

use strict;
use warnings FATAL => 'all';

my $REGION = "us-east-1";

# Make sure the AWS eb cli is installed
qx(eb --version) or die "Must install aws eb cli, see README.MD";

$ENV{AWS_PROFILE} = "default";

# Perform an eb status
print qx(eb status);
($? != 0) and die "Unable to execute eb status: $!\n";

my $env = "";
(@ARGV == 1) and $env = $ARGV[0];

print "Continue with deploy (y|n)? [y] ";
my $response = <STDIN>;
chomp($response);
die "Exiting" if ($response !~ /^y?$/i);

# Clean out the target of any previous build as we do not want to rebuild in the container
qx(lein clean);
($? != 0) and die "Unable to lein clean: $!\n";

# Deploy the project and let EB build + run the docker container
print qx(eb deploy $env);
($? != 0) and die "Unable to deploy: $!\n";
