interface UserIdentityLink {
  userId: string;
  type: string;
}

interface GroupIdentityLink {
  groupId: string;
  type: string;
}

export type IdentityLink = UserIdentityLink | GroupIdentityLink;
